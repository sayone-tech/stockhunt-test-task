package com.task.tradingAutomation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TradingAlertRequest {

    @JsonProperty("Name of strategy")
    private String strategyName;

    @JsonProperty("Symbol ID")
    private String symbolId;

    @JsonProperty("Action")
    private String action;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("Inverse")
    private boolean inverse;

    @JsonProperty("SL pertrade%")
    private float slPerTradePercent;

    @JsonProperty("Maxdayriskamount")
    private float maxDayRiskAmount;

}