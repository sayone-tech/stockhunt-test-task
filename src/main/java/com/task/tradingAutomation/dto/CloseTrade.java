package com.task.tradingAutomation.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CloseTrade {
    private String symbolId;
    private int quantity;

    public CloseTrade(String symbolId, int quantity) {
        this.symbolId = symbolId;
        this.quantity = quantity;
    }

}

