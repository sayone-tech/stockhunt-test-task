package com.task.tradingAutomation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlert;

@Service
public class OrderExecutionService {

    @Autowired
    private DhanBrokerApiClient dhanBrokerApiClient;

    public void executeOrder(TradingAlert tradingAlert) {
        // Implement the logic to execute the order using the Dhan API client
        // For example, place a market order or stop loss order based on the alert details
        if ("buy".equalsIgnoreCase(tradingAlert.getAction())) {
            dhanBrokerApiClient.placeBuyOrder(tradingAlert);
        } else if ("sell".equalsIgnoreCase(tradingAlert.getAction())) {
            dhanBrokerApiClient.placeSellOrder(tradingAlert);
        }
    }


}