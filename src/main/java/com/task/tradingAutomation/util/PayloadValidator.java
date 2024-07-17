package com.task.tradingAutomation.util;

import com.task.tradingAutomation.dto.TradingAlert;

public class PayloadValidator {
    public static boolean isValid(TradingAlert alert) {
        // Check if the alert object and its properties meet certain criteria
        return alert != null &&
                isValidString(alert.getNameOfStrategy()) &&
                isValidString(alert.getSymbolId()) &&
                isValidAction(alert.getAction()) &&
                alert.getQuantity() > 0 &&
                alert.getSlPerTrade() >= 0 &&  // Assuming SL per trade can be zero or positive
                alert.getMaxDayRiskAmount() >= 0;      // Assuming max day risk amount can be zero or positive
    }

    private static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static boolean isValidAction(String action) {
        return action != null && (action.equalsIgnoreCase("buy") || action.equalsIgnoreCase("sell"));
    }
}

