package com.task.tradingAutomation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.task.tradingAutomation.dto.*;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.enums.TradeStatus;
import com.task.tradingAutomation.exception.UserDefinedExceptions;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlertRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(OrderExecutionService.class);

    @Autowired
    private DhanBrokerApiClient dhanBrokerApiClient;

    @Autowired
    private RiskManagementService riskManagementService;

    @Autowired
    private TradeRepository tradeRepository;

    public void executeOrder(TradingAlertRequest tradingAlert) {


        riskManagementService.setMaxDayRiskAmount(tradingAlert);

        // Step 1: Apply business logic if inverse is true or false
        // Check if there are any existing trades to close
        Trades currentTrade = tradeRepository.findLatestBySymbolIdAndStatus(tradingAlert.getSymbolId(), TradeStatus.OPEN.toString());
        if (currentTrade != null) {

            if (tradingAlert.isInverse()) {  // If inverse is true, square off of existing and reopen positions with reversing action
                String oppositeAction = null;
                if ("buy".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "sell";
                } else if ("sell".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "buy";
                }
                tradingAlert.setAction(oppositeAction);
                closeCurrentPosition(currentTrade);
                openNewPosition(tradingAlert);// Method to open a new position based on the alert
            }else{
                // If inverse is false, simply square off positions
                closeCurrentPosition(currentTrade);
            }
        }else {
            //if no existing positions open a new one
            openNewPosition(tradingAlert);
        }

    }


    private void closeCurrentPosition(Trades currentTrade) {
        closeTrade(currentTrade);
        currentTrade.setStatus(TradeStatus.CLOSE);
        currentTrade.setUpdatedAt(LocalDateTime.now());
        tradeRepository.save(currentTrade);
    }
    public void openNewPosition(TradingAlertRequest tradingAlert) {
        // Create a new Trade object based on the alert parameters
        Trades newTrade = new Trades();
        newTrade.setSymbolId(tradingAlert.getSymbolId());
        newTrade.setAction(tradingAlert.getAction());
        newTrade.setStatus(TradeStatus.OPEN);
        newTrade.setQuantity(tradingAlert.getQuantity());
        newTrade.setStrategyName(tradingAlert.getStrategyName());
        newTrade.setSlPerTradePercent(tradingAlert.getSlPerTradePercent());
        newTrade.setCreatedAt(LocalDateTime.now());
        newTrade.setUpdatedAt(LocalDateTime.now());

        try {
            // Pass to order execution service for Dhan API interaction
            placeOrder(newTrade);
            // Save the new trade to the database
            tradeRepository.save(newTrade);

        } catch (Exception e) {
            // Log the exception and handle it as necessary
            logger.error("Error opening new position: " + e.getMessage());
            throw new RuntimeException("Failed to open new position", e);
        }
    }


    public void placeOrder(Trades newTrade) {
        try {
            float marketPrice = dhanBrokerApiClient.getCurrentMarketPrice(newTrade.getSymbolId()); // Get market price
            // Step 1: Place Buy Order
            if (newTrade.getAction().equalsIgnoreCase("buy")) {
                // Execute the buy order
                Map<String, Object>purchaseOrderMap = placeMarketOrder(newTrade, DhanOrderRequest.TransactionType.BUY.toString());
                String orderId = purchaseOrderMap.get("orderId").toString();
                newTrade.setOrderId(orderId);
                newTrade.setOrderStatus(purchaseOrderMap.get("orderStatus").toString());
                Map<String,Object>response = dhanBrokerApiClient.getTradeOrder(orderId);
                float entryPrice = (float) response.get("tradedPrice");

                // Per-Trade Risk Management - Calculate and set stop-loss price
                float stopLossPrice = riskManagementService.calculateStopLossPrice(newTrade.getSlPerTradePercent(),
                        entryPrice,newTrade.getAction());
                float buffer = 0.01f;
                float bufferedStopLossPrice = stopLossPrice * (1 - buffer);
                newTrade.setStopLossPrice(bufferedStopLossPrice);
                placeStopLossOrder(newTrade);
            } else if (newTrade.getAction().equalsIgnoreCase("sell")) {
                // Place sell order
                Map<String, Object>sellOrderMap =placeMarketOrder(newTrade, DhanOrderRequest.TransactionType.SELL.toString());
                newTrade.setOrderId(sellOrderMap.get("orderId").toString());
                newTrade.setOrderStatus(sellOrderMap.get("orderStatus").toString());

                // Risk Management - Calculate and set stop-loss for the sell position
                float stopLossPrice = riskManagementService.calculateStopLossPrice(newTrade.getSlPerTradePercent(),
                        marketPrice,newTrade.getAction());
                float buffer = 0.01f;
                float bufferedStopLossPrice = stopLossPrice * (1 + buffer); // Increase for short protection
                newTrade.setStopLossPrice(bufferedStopLossPrice);
                placeStopLossOrder(newTrade);
            }

            // Monitor and Manage Risk -update total risk and check
            riskManagementService.monitorAndManageRisk();


        } catch (Exception e) {
            // Log the exception and handle it as necessary
            System.err.println("Error placing order: " + e.getMessage());
            throw new UserDefinedExceptions.TradeNotPlacedException("Trade order placement failed");
        }
    }

    private Map<String, Object> placeMarketOrder(Trades trade, String actionType) throws UserDefinedExceptions.TradeNotPlacedException {
        trade.setOrderType(DhanOrderRequest.OrderType.MARKET.toString());
        trade.setAction(actionType);
        Map<String, Object> response = null;
        try {
            response = dhanBrokerApiClient.placeTradeOrder(trade);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (!response.get("status").equals("Success")) {
            throw new UserDefinedExceptions.TradeNotPlacedException("Trade order placement failed");
        }
        return response;
    }

    private void placeStopLossOrder(Trades trade) throws UserDefinedExceptions.TradeNotPlacedException {
        trade.setOrderType(DhanOrderRequest.OrderType.STOP_LOSS.toString());
        Map<String, Object> response = null;
        try {
            response = dhanBrokerApiClient.placeTradeOrder(trade);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (!response.get("status").equals("Success")) {
            throw new UserDefinedExceptions.TradeNotPlacedException("Stop-loss order placement failed");
        }
    }


    public void closeTrade(Trades trade) {
        try {
            // Assuming Dhan API has a method to close single trade
            dhanBrokerApiClient.closeTrade(trade.getSymbolId(), trade.getQuantity());

            // Update the trade status and timestamp
            trade.setStatus(TradeStatus.CLOSE);
            trade.setUpdatedAt(LocalDateTime.now());

            // Save the updated status trade to the repository
            tradeRepository.save(trade);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error closing trade for symbol: " + trade.getSymbolId() + ", " + e.getMessage());
        }
    }


}