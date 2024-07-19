package com.task.tradingAutomation.util;

import com.task.tradingAutomation.dto.TradingAlertRequest;

public class PayloadValidator {
    public static boolean isValid(TradingAlertRequest alert) {
        // Check if the alert object and its properties meet certain criteria
        return alert != null &&
                isValidString(alert.getStrategyName()) &&
                isValidString(alert.getSymbolId()) &&
                isValidAction(alert.getAction()) &&
                alert.getQuantity() > 0 &&
                alert.getSlPerTradePercent() >= 0 &&  // Assuming SL per trade can be zero or positive
                alert.getMaxDayRiskAmount() >= 0;      // Assuming max day risk amount can be zero or positive
    }

    private static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static boolean isValidAction(String action) {
        return action != null && (action.equalsIgnoreCase("buy") || action.equalsIgnoreCase("sell"));
    }
}

