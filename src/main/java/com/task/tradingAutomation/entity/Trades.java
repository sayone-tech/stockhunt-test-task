package com.task.tradingAutomation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Trades")
public class Trades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_name", nullable = false)
    private String strategyName;

    @Column(name = "symbol_id", nullable = false)
    private String symbolId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "inverse", nullable = false)
    private boolean inverse;

    @Column(name = "sl_pertrade_percent", nullable = false)
    private float slPerTradePercent;

    @Column(name = "entry_price", nullable = false)
    private float entryPrice;

    @Column(name = "stop_loss_price")
    private Float stopLossPrice;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



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

    public float getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(float entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Float getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(Float stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
