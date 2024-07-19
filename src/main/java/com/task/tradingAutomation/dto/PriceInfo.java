package com.task.tradingAutomation.dto;

public class PriceInfo {
    private double marketPrice;
    private double stopLossPrice;

    public PriceInfo(double marketPrice, double stopLossPrice) {
        this.marketPrice = marketPrice;
        this.stopLossPrice = stopLossPrice;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public double getStopLossPrice() {
        return stopLossPrice;
    }
}
