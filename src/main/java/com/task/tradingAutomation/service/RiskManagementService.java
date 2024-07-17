package com.task.tradingAutomation.service;

import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlert;

@Service
public class RiskManagementService {

    public boolean isTradeAllowed(TradingAlert tradingAlert) {
        // Implement the risk management logic
        // Example: Check if the trade complies with the maximum daily risk limit
        float maxDayRiskAmount = tradingAlert.getMaxDayRiskAmount();
        float currentDayRisk = calculateCurrentDayRisk();
        return (currentDayRisk + calculateTradeRisk(tradingAlert)) <= maxDayRiskAmount;
    }

    private float calculateCurrentDayRisk() {
        // Implement logic to calculate the current day's total risk
        return 0; // Placeholder for actual implementation
    }

    // Track cumulative daily risk
    private float cumulativeRisk = 0.0f;

    public void manageRisk(TradingAlert alert) {
        //  per-trade risk management
        float risk = calculateTradeRisk(alert);
        if (cumulativeRisk + risk > alert.getMaxDayRiskAmount()) {
            throw new RuntimeException("Daily risk limit reached");
        }

        cumulativeRisk += risk;
        // Implement additional risk management logic if needed

        // Additional logic: Close open trades if necessary
        if (cumulativeRisk >= alert.getMaxDayRiskAmount()) {
            // Implement logic to close open trades
//            closeOpenTrades();
        }
    }

    private float calculateTradeRisk(TradingAlert tradingAlert) {
        // logic to calculate the risk for the given trade
        return tradingAlert.getQuantity() * tradingAlert.getSlPerTrade();
    }

//    private void closeOpenTrades() {
//        // Logic to close open trades goes here
//        // Example:
//        for (Trade trade : openTrades) {
//            trade.close();
//        }
//        openTrades.clear(); // Clear open trades list after closing
//    }
}