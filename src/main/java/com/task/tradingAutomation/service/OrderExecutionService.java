package com.task.tradingAutomation.service;

import com.task.tradingAutomation.Entity.Trades;
import com.task.tradingAutomation.exception.UserDefinedExceptions;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlert;

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

    public void executeOrder(TradingAlert tradingAlert) {


        riskManagementService.getMaxDayRiskAmount(tradingAlert);

        // Determine the order type based on the alert details
        String orderType = determineOrderType(tradingAlert);
        tradingAlert.setOrderType(orderType);


        // Step 1: Apply business logic if inverse is true or false
        // Check if there are any existing trades to close
        Trades currentTrade = tradeRepository.findLatestBySymbolIdAndStatus(tradingAlert.getSymbolId(),"open");
        if (currentTrade != null) {

            if (tradingAlert.isInverse()) {  // If inverse is true, square off of existing and reopen positions with reversing action
                closeCurrentPosition(currentTrade);
                String oppositeAction = null;
                if ("buy".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "sell";
                } else if ("sell".equalsIgnoreCase(currentTrade.getAction())) {
                    oppositeAction = "buy";
                }
                tradingAlert.setAction(oppositeAction);
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
        currentTrade.setStatus("closed");
        currentTrade.setUpdatedAt(LocalDateTime.now());
        tradeRepository.save(currentTrade);
    }
    public void openNewPosition(TradingAlert tradingAlert) {
        // Create a new Trade object based on the alert parameters
        Trades newTrade = new Trades();
        newTrade.setSymbolId(tradingAlert.getSymbolId());
        newTrade.setAction(tradingAlert.getAction());
        newTrade.setStatus("open");
        newTrade.setQuantity(tradingAlert.getQuantity());
        newTrade.setCreatedAt(LocalDateTime.now());
        newTrade.setUpdatedAt(LocalDateTime.now());
        newTrade.setStrategyName(tradingAlert.getNameOfStrategy());
        newTrade.setSlPerTradePercent(tradingAlert.getSlPerTrade());

        try {
            // Save the new trade to the database
            tradeRepository.save(newTrade);

            // Copy properties from newTrade to tradingAlert for order execution
            TradingAlert orderAlert = new TradingAlert();
            BeanUtils.copyProperties(newTrade, orderAlert);

            // Pass to order execution service for Dhan API interaction
            placeOrder(orderAlert);
        } catch (Exception e) {
            // Log the exception and handle it as necessary
            logger.error("Error opening new position: " + e.getMessage());
            throw new RuntimeException("Failed to open new position", e);
        }
    }


    public void placeOrder(TradingAlert tradingAlert) {
        try {
            // Step 1: Per-Trade Risk Management - Calculate and set stop loss price
            double stopLossPrice = riskManagementService.calculateStopLossPrice(tradingAlert);
            tradingAlert.setPrice(tradingAlert.getOrderType().equals("market") ? 0 : stopLossPrice);

            // Step 2: Check if trade is allowed based on risk management rules
            if (!riskManagementService.isTradeAllowed(tradingAlert,stopLossPrice)) {
                throw new UserDefinedExceptions.TradeNotAllowedException("Trade not allowed based on risk management rules");
            }

            // Step 3: Execute the order using Dhan API client
            dhanBrokerApiClient.placeTradeOrder(tradingAlert);

            // Update the cumulative risk
            riskManagementService.manageDailyRisk(tradingAlert);

        } catch (Exception e) {
            // Log the exception and handle it as necessary
            System.err.println("Error placing order: " + e.getMessage());
            throw new RuntimeException("Failed to place order", e);
        }
    }


    private String determineOrderType(TradingAlert tradingAlert) {
        if (tradingAlert.getSlPerTrade() > 0) {
            return "stop_loss";
        }
        return "market";
    }


}