package com.task.tradingAutomation.dto;

import com.task.tradingAutomation.entity.Trades;

import java.util.List;

public class CloseTradesRequest {
    private List<Trades> trades;

    public CloseTradesRequest(List<Trades> trades) {
        this.trades = trades;
    }

    public List<Trades> getTrades() {
        return trades;
    }

    public void setTrades(List<Trades> trades) {
        this.trades = trades;
    }


}
