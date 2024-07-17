package com.task.tradingAutomation.service;

import com.task.tradingAutomation.Entity.Trades;
import com.task.tradingAutomation.dto.TradingAlert;
import com.task.tradingAutomation.repository.TradeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebhookListenerService {

    @Autowired
    private OrderExecutionService orderExecutionService;

    @Autowired
    private RiskManagementService riskManagementService;

    @Autowired
    private TradeRepository tradeRepository;


    public void processAlert(TradingAlert tradingAlert) {
        // Step 1: Apply business logic if inverse is true or false
        // Check if there are any existing trades to close
        Trades currentTrade = tradeRepository.findLatestBySymbolId(tradingAlert.getSymbolId());
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

        // Determine the order type based on the alert details
        String orderType = determineOrderType(tradingAlert);
        tradingAlert.setOrderType(orderType);

        // Step 2: Perform risk management checks
        if (!riskManagementService.isTradeAllowed(tradingAlert)) {
            throw new IllegalStateException("Trade not allowed based on risk management rules");
        }

        // Step 3: Execute the order
        orderExecutionService.executeOrder(tradingAlert);
    }


    private void closeCurrentPosition(Trades currentTrade) {
            currentTrade.setStatus("closed");
            currentTrade.setUpdatedAt(LocalDateTime.now());
            tradeRepository.save(currentTrade);
    }
    private void openNewPosition(TradingAlert tradingAlert) {

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

        // Save the new trade to the database
        tradeRepository.save(newTrade);

        //pass to order execution service for dhan api interaction
        BeanUtils.copyProperties(newTrade, tradingAlert);
        orderExecutionService.executeOrder(tradingAlert);

    }


    private String determineOrderType(TradingAlert tradingAlert) {
        if (tradingAlert.getSlPerTrade() > 0) {
            return "stop_loss";
        }
        return "market";
    }
}