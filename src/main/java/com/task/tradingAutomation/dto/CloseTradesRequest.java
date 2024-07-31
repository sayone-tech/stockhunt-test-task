package com.task.tradingAutomation.dto;

import com.task.tradingAutomation.entity.Trades;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class CloseTradesRequest {
    private List<Trades> trades;

    public CloseTradesRequest(List<Trades> trades) {
        this.trades = trades;
    }

}
