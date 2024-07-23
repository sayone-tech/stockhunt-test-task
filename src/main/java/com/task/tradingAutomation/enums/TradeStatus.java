package com.task.tradingAutomation.enums;

public enum TradeStatus {
    OPEN("open"),
    CLOSE("close");

    private String status;

    TradeStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

