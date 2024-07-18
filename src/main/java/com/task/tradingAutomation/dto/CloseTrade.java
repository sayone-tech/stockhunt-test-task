package com.task.tradingAutomation.dto;

public class CloseTrade {
    private String symbolId;
    private int quantity;

    public CloseTrade(String symbolId, int quantity) {
        this.symbolId = symbolId;
        this.quantity = quantity;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

