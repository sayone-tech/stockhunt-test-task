package com.task.tradingAutomation.dto;

public class DhanOrderRequest {
    private String dhanClientId;
    private TransactionType transactionType;
    private ExchangeSegment exchangeSegment;
    private ProductType productType;
    private OrderType orderType;
    private Validity validity;
    private String securityId;
    private int quantity;
    private float price; // Use 0 for market orders
    private float triggerPrice;

    // Enum definitions
    public enum TransactionType {
        BUY, SELL
    }

    public enum ExchangeSegment {
        NSE_EQ, NSE_FNO, NSE_CURRENCY, BSE_EQ, BSE_FNO, BSE_CURRENCY, MCX_COMM
    }

    public enum ProductType {
        CNC, INTRADAY, MARGIN, MTF, CO, BO
    }

    public enum OrderType {
        LIMIT, MARKET, STOP_LOSS, STOP_LOSS_MARKET
    }

    public enum Validity {
        DAY, IOC
    }


    public String getDhanClientId() {
        return dhanClientId;
    }

    public void setDhanClientId(String dhanClientId) {
        this.dhanClientId = dhanClientId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public ExchangeSegment getExchangeSegment() {
        return exchangeSegment;
    }

    public void setExchangeSegment(ExchangeSegment exchangeSegment) {
        this.exchangeSegment = exchangeSegment;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(float triggerPrice) {
        this.triggerPrice = triggerPrice;
    }
}
