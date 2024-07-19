package com.task.tradingAutomation.service;

import com.task.tradingAutomation.dto.OrderAlert;
import com.task.tradingAutomation.dto.TradingAlertRequest;
import com.task.tradingAutomation.entity.RiskData;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.dto.PriceInfo;
import com.task.tradingAutomation.repository.RiskDataRepository;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RiskManagementService.class);

    @Autowired
    DhanBrokerApiClient dhanBrokerApiClient;

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    private RiskDataRepository riskDataRepository;


    private double maxDayRiskAmount;

    public void setMaxDayRiskAmount(TradingAlertRequest tradingAlert) {
        maxDayRiskAmount =tradingAlert.getMaxDayRiskAmount();
    }
    public double getMaxDayRiskAmount() {
        return maxDayRiskAmount;
    }

    //Per-Trade get stop loss
    public PriceInfo calculateStopLossPrice(OrderAlert tradingAlert) {
        try {
            if (tradingAlert == null) {
                throw new IllegalArgumentException("TradingAlert cannot be null.");
            }
            double marketPrice = dhanBrokerApiClient.getCurrentMarketPrice(tradingAlert.getSymbolId());//get market price
//            double marketPrice = 150;//dummy value
            double stopLossPrice = marketPrice - (marketPrice * tradingAlert.getSlPerTradePercent() / 100);// Calculate stop loss price
            return new PriceInfo(marketPrice, stopLossPrice);
        }   catch (Exception e) {
            logger.error("Error calculating stop loss: " + e.getMessage());
            return new PriceInfo(Double.NaN, Double.NaN); // NaN (Not-a-Number) or another default value indicating failure
        }
    }


    // Track cumulative daily risk
    private float cumulativeRisk;

    public void manageDailyRisk(OrderAlert alert,float tradeRisk) {
        if (cumulativeRisk + tradeRisk > maxDayRiskAmount) {
            throw new RuntimeException("Daily risk limit reached");
        }
        cumulativeRisk += tradeRisk;
        if (cumulativeRisk >= maxDayRiskAmount) { // Close open trades if necessary
            closeOpenTrades();
        }
        updateRiskData(cumulativeRisk);
    }

    private void updateRiskData(float cumulativeRisk) {
        RiskData riskData = new RiskData();
        // Set the values
        riskData.setCumulativeRisk(cumulativeRisk);
        riskData.setDate(LocalDateTime.now());
        // Save the RiskData for next evaluation
        riskDataRepository.save(riskData);
    }


    public float calculateCurrentDayRisk() {
        return riskDataRepository.findLatestCumulativeRisk()
                .orElse(0.0f); // Return 0 if no cumulative risk found
    }

    //check if trade allowed based on risk management
    public boolean isTradeAllowed( float tradeRisk) {
        // Check if the trade complies with the maximum daily risk limit
        float currentDayRisk = calculateCurrentDayRisk();
        return (currentDayRisk + tradeRisk ) <= maxDayRiskAmount;// current trade risk + current day risk <= maxDayRiskAmount
    }


    // Per-Trade risk management: Calculate the risk amount based on stop loss percentage
    public float calculateTradeRisk(OrderAlert alert) {
        double slPerTradePercent = alert.getSlPerTradePercent(); // Get stop loss percentage
        float riskAmount = alert.getQuantity() * (float) (slPerTradePercent / 100.0);// Calculate risk amount as quantity multiplied by stop loss percentage
        return riskAmount;
    }

    public void closeOpenTrades() {
        List<Trades> openTrades = tradeRepository.findByStatus("open");
        dhanBrokerApiClient.closeAllOpenTrades(openTrades);//get the api to close all open trades
        for (Trades trade : openTrades) {
            try {
                trade.setStatus("closed");
                tradeRepository.save(trade);
            } catch (Exception e) {
                logger.error("Error closing trade for symbol: " + trade.getSymbolId() + ", " + e.getMessage());
            }
        }
        System.out.println("Closed all open trades to limit risk.");
    }

}