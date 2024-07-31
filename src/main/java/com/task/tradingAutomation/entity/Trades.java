package com.task.tradingAutomation.entity;

import com.task.tradingAutomation.enums.TradeStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "Trades")
@Data
@NoArgsConstructor
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
    private float stopLossPrice;

    @Column(name = "order_type")
    private String orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @Column(name = "order_id", nullable = false)
    private String orderId;
}