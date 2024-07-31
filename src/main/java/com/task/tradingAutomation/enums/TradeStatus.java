package com.task.tradingAutomation.enums;

import lombok.Getter;

@Getter
public enum TradeStatus {
    OPEN("open"),
    CLOSE("close");

    private String status;

    TradeStatus(String status) {
        this.status = status;
    }
}

