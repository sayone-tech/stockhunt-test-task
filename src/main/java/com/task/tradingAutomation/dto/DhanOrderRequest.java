package com.task.tradingAutomation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

}
