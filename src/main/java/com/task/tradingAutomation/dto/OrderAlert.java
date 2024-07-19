package com.task.tradingAutomation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderAlert {

    private Long id;
    private String strategyName;
    private String symbolId;
    private String action;
    private int quantity;
    private boolean inverse;
    private float slPerTradePercent;
    private float maxDayRiskAmount;
    private String orderType; // "market" or "stop_loss"
    private double stopLossPrice; // Use for stop loss orders


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
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

    public float getSlPerTradePercent() {
        return slPerTradePercent;
    }

    public void setSlPerTradePercent(float slPerTradePercent) {
        this.slPerTradePercent = slPerTradePercent;
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

    public double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }
}
