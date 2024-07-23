package com.task.tradingAutomation.service;

import com.task.tradingAutomation.config.TradingConfig;
import com.task.tradingAutomation.dto.TradingAlertRequest;
import com.task.tradingAutomation.entity.RiskData;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.enums.TradeStatus;
import com.task.tradingAutomation.repository.RiskDataRepository;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private TradingConfig tradingConfig;


    private double maxDayRiskAmount;

    public void setMaxDayRiskAmount(TradingAlertRequest tradingAlert) {
        maxDayRiskAmount =tradingAlert.getMaxDayRiskAmount();
    }
    public double getMaxDayRiskAmount() {
        return maxDayRiskAmount;
    }

    //Per-Trade get stop loss
    public float calculateStopLossPrice(float marketPrice,float slPerTradePercent,String transactionType) {
        try {
            float stopLossPrice = 0.0f;
            if(transactionType.equalsIgnoreCase("buy")) {
                stopLossPrice = marketPrice - (marketPrice * slPerTradePercent / 100);// Calculate stop loss price
            }else {
                stopLossPrice = marketPrice + (marketPrice * slPerTradePercent / 100);
            }
            return  stopLossPrice;
        }   catch (Exception e) {
            logger.error("Error calculating stop loss: " + e.getMessage());
            return Float.NaN;  // NaN (Not-a-Number) or another default value indicating failure
        }
    }


    private void updateRiskData(float cumulativeRisk) {
        RiskData riskData = new RiskData();
        // Set the values
        riskData.setCumulativeRisk(cumulativeRisk);
        riskData.setDate(LocalDateTime.now());
        // Save the RiskData for next evaluation
        riskDataRepository.save(riskData);
    }



    public float calculateTotalRisk() {
        float totalRisk = 0.0f;
        List<Trades> openTrades = tradeRepository.findByStatus(TradeStatus.OPEN);
        try {
            for (Trades trade : openTrades) {
                float tradeRisk = calculateTradeRisk(trade);
                totalRisk += tradeRisk;
            }
            updateRiskData(totalRisk);
            logger.info("Total calculated risk: ${}", totalRisk);
        } catch (Exception e) {
            logger.error("Error calculating total risk: " + e.getMessage(), e);
        }
        return totalRisk;
    }


    public float calculateTradeRisk(Trades trade) {
        float entryPrice = trade.getEntryPrice();
        float stopLossPrice = trade.getStopLossPrice();
        int quantity = trade.getQuantity();
        float riskPerShare;
        if ("SELL".equalsIgnoreCase(trade.getAction())) {
            riskPerShare = stopLossPrice - entryPrice; // For short positions
        } else {
            riskPerShare = entryPrice - stopLossPrice; // For long positions
        }

        float tradeRisk = riskPerShare * quantity;

        return tradeRisk;
    }


    public void closeOpenTrades() {
        List<Trades> openTrades = tradeRepository.findByStatus(TradeStatus.OPEN);
        dhanBrokerApiClient.closeAllOpenTrades(openTrades);//get the api to close all open trades
        for (Trades trade : openTrades) {
            try {
                trade.setStatus(TradeStatus.CLOSE);
                tradeRepository.save(trade);
            } catch (Exception e) {
                logger.error("Error closing trade for symbol: " + trade.getSymbolId() + ", " + e.getMessage());
            }
        }
        System.out.println("Closed all open trades to limit risk.");
    }

    public void monitorAndManageRisk() {
        try {
            // Calculate total risk exposure
            float totalRisk = calculateTotalRisk();

            // Compare with maximum daily risk amount
            if (totalRisk > maxDayRiskAmount) {
                logger.warn("Total risk exposure exceeds the maximum daily risk amount of ${}", maxDayRiskAmount);
                // Implement corrective actions
                takeCorrectiveActions();
            } else {
                logger.info("Total risk exposure is within acceptable limits: ${}", totalRisk);
            }
        } catch (Exception e) {
            logger.error("Error monitoring and managing risk: " + e.getMessage(), e);
        }
    }

    private void takeCorrectiveActions() {
        try {
            // Implement corrective actions such as halting new trades or notifying users
            logger.info("Taking corrective actions due to high risk exposure.");
            // Example action: Closing all open trades
            List<Trades> openTrades = tradeRepository.findByStatus(TradeStatus.OPEN);
            dhanBrokerApiClient.closeAllOpenTrades(openTrades);
            // Disable new trades
            disableNewTrades();
            // Alert the user
            alertUser();
        } catch (Exception e) {
            // Log the exception
            logger.error("Error taking corrective actions: " + e.getMessage(), e);
            // Optionally, rethrow the exception to propagate it
            throw new RuntimeException("Failed to take corrective actions due to an error", e);
        }
    }

    private void disableNewTrades() {
        // Logic to disable new trades
        logger.info("New trades have been disabled.");
        tradingConfig.setTradingEnabled(false);

    }

    private void alertUser() {
        // Logic to alert the user about high risk exposure
        logger.info("User has been alerted about the high risk exposure.");
        // Example: Send an email or push notification to the user
    }


}