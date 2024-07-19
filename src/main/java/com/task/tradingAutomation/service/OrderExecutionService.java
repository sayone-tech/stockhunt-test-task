package com.task.tradingAutomation.service;

import com.task.tradingAutomation.dto.OrderAlert;
import com.task.tradingAutomation.dto.TradingAlertRequest;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.dto.PriceInfo;
import com.task.tradingAutomation.exception.UserDefinedExceptions;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlertRequest;

import java.time.LocalDateTime;

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
        Trades currentTrade = tradeRepository.findLatestBySymbolIdAndStatus(tradingAlert.getSymbolId(),"open");
        if (currentTrade != null) {

            if (tradingAlert.isInverse()) {  // If inverse is true, square off of existing and reopen positions with reversing action
                String oppositeAction = null;
                if ("buy".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "sell";
                } else if ("sell".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "buy";
                }
                tradingAlert.setAction(oppositeAction);
                openNewPosition(tradingAlert);// Method to open a new position based on the alert
                closeCurrentPosition(currentTrade);
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
        currentTrade.setStatus("closed");
        currentTrade.setUpdatedAt(LocalDateTime.now());
        tradeRepository.save(currentTrade);
    }
    public void openNewPosition(TradingAlertRequest tradingAlert) {
        // Create a new Trade object based on the alert parameters
        Trades newTrade = new Trades();
        newTrade.setSymbolId(tradingAlert.getSymbolId());
        newTrade.setAction(tradingAlert.getAction());
        newTrade.setStatus("open");
        newTrade.setQuantity(tradingAlert.getQuantity());
        newTrade.setStrategyName(tradingAlert.getStrategyName());
        newTrade.setSlPerTradePercent(tradingAlert.getSlPerTradePercent());
        newTrade.setCreatedAt(LocalDateTime.now());
        newTrade.setUpdatedAt(LocalDateTime.now());

        try {

            // Copy properties from newTrade to tradingAlert for order execution
            OrderAlert orderAlert = new OrderAlert();
            BeanUtils.copyProperties(newTrade, orderAlert);

            // Pass to order execution service for Dhan API interaction
            // Determine the order type based on the alert details
            String orderType = determineOrderType(tradingAlert);
            orderAlert.setOrderType(orderType);
            placeOrder(orderAlert);
            // Save the new trade to the database
            tradeRepository.save(newTrade);

        } catch (Exception e) {
            // Log the exception and handle it as necessary
            logger.error("Error opening new position: " + e.getMessage());
            throw new RuntimeException("Failed to open new position", e);
        }
    }


    public void placeOrder(OrderAlert orderAlert) {
        try {
            // Step 1: Per-Trade Risk Management - Calculate and set stop loss price
            PriceInfo priceInfo = riskManagementService.calculateStopLossPrice(orderAlert);
            // Step 2: Check if trade is allowed based on stop loss and current market value
            if (!isTradeAllowedWithStopLoss(orderAlert, priceInfo.getStopLossPrice(), priceInfo.getMarketPrice())) {
                throw new UserDefinedExceptions.TradeNotAllowedException("Trade not allowed based on risk management rules");
            }
            orderAlert.setStopLossPrice(orderAlert.getOrderType().equals("market") ? 0 : priceInfo.getStopLossPrice());

            //Calculate trade risk once
            float tradeRisk = riskManagementService.calculateTradeRisk(orderAlert);
            // Step 2: Check if trade is allowed based on risk management rules
            if (!riskManagementService.isTradeAllowed(tradeRisk)) {
                throw new UserDefinedExceptions.TradeNotAllowedException("Trade not allowed based on risk management rules");
            }

            // Step 3: Execute the order using Dhan API client
            dhanBrokerApiClient.placeTradeOrder(orderAlert);

            // Update the cumulative risk
            riskManagementService.manageDailyRisk(orderAlert,tradeRisk);

        } catch (Exception e) {
            // Log the exception and handle it as necessary
            System.err.println("Error placing order: " + e.getMessage());
            throw new RuntimeException("Failed to place order", e);
        }
    }

    private boolean isTradeAllowedWithStopLoss(OrderAlert tradingAlert,double stopLossPrice,double marketPrice) {
        // Check if the stop loss price is valid based on the current market price
        marketPrice = 150;//dummy value
        if (tradingAlert.getAction().equalsIgnoreCase("buy") && marketPrice <= stopLossPrice) {
            return false;
        } else if (tradingAlert.getAction().equalsIgnoreCase("sell") && marketPrice >= stopLossPrice) {
            return false;
        }
        return true;
    }


    private String determineOrderType(TradingAlertRequest tradingAlert) {
        if (tradingAlert.getSlPerTradePercent() > 0) {
            return "stop_loss";
        }
        return "market";
    }

    public void closeTrade(Trades trade) {
        try {
            // Assuming Dhan API has a method to close single trade
            dhanBrokerApiClient.closeTrade(trade.getSymbolId(), trade.getQuantity());

            // Update the trade status and timestamp
            trade.setStatus("closed");
            trade.setUpdatedAt(LocalDateTime.now());

            // Save the updated status trade to the repository
            tradeRepository.save(trade);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error closing trade for symbol: " + trade.getSymbolId() + ", " + e.getMessage());
        }
    }


}