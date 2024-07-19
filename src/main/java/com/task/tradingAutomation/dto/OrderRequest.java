package com.task.tradingAutomation.dto;

public class OrderRequest {
    private String nameOfStrategy;
    private String symbolId;
    private int quantity;
    private String action; // "buy" or "sell"
    private String orderType; // "market" or "stop_loss"
    private double price; // Use 0 for market orders

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
