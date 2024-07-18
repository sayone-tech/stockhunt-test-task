package com.task.tradingAutomation.service;

import com.task.tradingAutomation.Entity.Trades;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.task.tradingAutomation.dto.TradingAlert;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RiskManagementService.class);

    @Autowired
    DhanBrokerApiClient dhanBrokerApiClient;

    @Autowired
    TradeRepository tradeRepository;


    private double maxDayRiskAmount;

    public void getMaxDayRiskAmount(TradingAlert tradingAlert) {
        maxDayRiskAmount =tradingAlert.getMaxDayRiskAmount();
    }
    //Per-Trade get stop loss
    public double calculateStopLossPrice(TradingAlert tradingAlert) {
        try {
            if (tradingAlert == null) {
                throw new IllegalArgumentException("TradingAlert cannot be null.");
            }
            double entryPrice = dhanBrokerApiClient.getCurrentMarketPrice(tradingAlert.getSymbolId());//get market price
            double stopLossPrice = entryPrice - (entryPrice * tradingAlert.getSlPerTrade() / 100);// Calculate stop loss price
            return stopLossPrice;
        } catch (Exception e) {
            logger.error("Error calculating stop loss: " + e.getMessage());
            return Double.NaN; // NaN (Not-a-Number) or another default value indicating failure
        }
    }


    // Track cumulative daily risk
    private float cumulativeRisk = 0.0f;

    public void manageDailyRisk(TradingAlert alert) {
        //  per-trade risk management
        float risk = calculateTradeRisk(alert);
        if (cumulativeRisk + risk > alert.getMaxDayRiskAmount()) {
            throw new RuntimeException("Daily risk limit reached");
        }
        cumulativeRisk += risk;

        if (cumulativeRisk >= alert.getMaxDayRiskAmount()) { // Close open trades if necessary
            closeOpenTrades();
        }

    }

    private float calculateCurrentDayRisk() {
        return cumulativeRisk;
    }

    public boolean isTradeAllowed(TradingAlert tradingAlert,double stopLossPrice) {
        // Check if the stop loss price is valid based on the current market price
        double currentPrice = dhanBrokerApiClient.getCurrentMarketPrice(tradingAlert.getSymbolId());
        if (tradingAlert.getAction().equalsIgnoreCase("buy") && currentPrice <= stopLossPrice) {
            return false;
        } else if (tradingAlert.getAction().equalsIgnoreCase("sell") && currentPrice >= stopLossPrice) {
            return false;
        }
        // Implement the risk management logic
        // Example: Check if the trade complies with the maximum daily risk limit
        float currentDayRisk = calculateCurrentDayRisk();
        return (currentDayRisk + calculateTradeRisk(tradingAlert)) <= maxDayRiskAmount;
    }



    // Per-Trade risk management: Calculate the risk amount based on stop loss percentage
    private float calculateTradeRisk(TradingAlert tradingAlert) {
        double slPerTradePercent = tradingAlert.getSlPerTrade(); // Get stop loss percentage

        // Calculate risk amount as quantity multiplied by stop loss percentage
        float riskAmount = tradingAlert.getQuantity() * (float) (slPerTradePercent / 100.0);
        return riskAmount;
    }

    private void closeOpenTrades() {

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


    // Scheduled job to monitor trades every minute -Periodically checks if any open trades have hit their stop loss
    // or if cumulative risk requires closing all trades
    @Scheduled(fixedRate = 60000) // Check every minute
    public void monitorTrades() {

        List<Trades> openTrades = tradeRepository.findByStatus("open");
        for (Trades trade : openTrades) {
            double currentPrice = dhanBrokerApiClient.getCurrentMarketPrice(trade.getSymbolId());
            if (trade.getAction().equalsIgnoreCase("buy") && currentPrice <= trade.getStopLossPrice()) {
                closeTrade(trade);
            } else if (trade.getAction().equalsIgnoreCase("sell") && currentPrice >= trade.getStopLossPrice()) {
                closeTrade(trade);
            }
        }
        if (cumulativeRisk > maxDayRiskAmount) {
            closeOpenTrades();
        }
    }

    private void closeTrade(Trades trade) {
        try {
            // Assuming Dhan API has a method to close trades
            dhanBrokerApiClient.closeTrade(trade.getSymbolId(), trade.getQuantity());

            // Update the trade status and timestamp
            trade.setStatus("closed");
            trade.setUpdatedAt(LocalDateTime.now());

            // Save the updated trade to the repository
            tradeRepository.save(trade);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error closing trade for symbol: " + trade.getSymbolId() + ", " + e.getMessage());
        }
    }

}