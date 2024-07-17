package com.task.tradingAutomation.dto;

public class TradingAlert {
    private String nameOfStrategy;
    private String symbolId;
    private String action;
    private int quantity;
    private boolean inverse;
    private float slPerTrade;
    private float maxDayRiskAmount;
    private String orderType; // "market" or "stop_loss"
    private double price; // Use for stop loss orders

    public String getNameOfStrategy() {
        return nameOfStrategy;
    }

    public void setNameOfStrategy(String nameOfStrategy) {
        this.nameOfStrategy = nameOfStrategy;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public float getSlPerTrade() {
        return slPerTrade;
    }

    public void setSlPerTrade(float slPerTrade) {
        this.slPerTrade = slPerTrade;
    }

    public float getMaxDayRiskAmount() {
        return maxDayRiskAmount;
    }

    public void setMaxDayRiskAmount(float maxDayRiskAmount) {
        this.maxDayRiskAmount = maxDayRiskAmount;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}