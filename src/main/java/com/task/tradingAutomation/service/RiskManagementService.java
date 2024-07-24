package com.task.tradingAutomation.service;

import com.task.tradingAutomation.config.TradingConfig;
import com.task.tradingAutomation.dto.DhanOrderRequest;
import com.task.tradingAutomation.dto.TradingAlertRequest;
import com.task.tradingAutomation.entity.RiskData;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.enums.TradeStatus;
import com.task.tradingAutomation.exception.UserDefinedExceptions;
import com.task.tradingAutomation.repository.RiskDataRepository;
import com.task.tradingAutomation.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

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


    private float maxDayRiskAmount;

    public void setMaxDayRiskAmount(TradingAlertRequest tradingAlert) {
        maxDayRiskAmount =tradingAlert.getMaxDayRiskAmount();
    }
    public float getMaxDayRiskAmount() {
        return  maxDayRiskAmount;
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



    //update risk data
    public void updateRiskData(float tradeRisk) {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(LocalTime.MAX);

        Optional<Float> latestRiskOpt = riskDataRepository.findLatestCumulativeRiskForToday(startOfDay, endOfDay);

        RiskData riskData = new RiskData();
        riskData.setDate(LocalDateTime.now());

        float updatedRisk;
        if (latestRiskOpt.isPresent()) {
            // Update existing risk data
            updatedRisk = latestRiskOpt.get() + tradeRisk;
        } else {
            // Create a new risk data entry for today
            updatedRisk = tradeRisk;
        }

        riskData.setCumulativeRisk(updatedRisk);

        // Save the RiskData
        riskDataRepository.save(riskData);
    }

    //to get the latest cumulative risk of the day
    public float calculateCurrentDayRisk() {
        LocalDateTime startOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now(ZoneId.systemDefault()).with(LocalTime.MAX);

        return riskDataRepository.findLatestCumulativeRiskForToday(startOfDay, endOfDay)
                .orElse(0.0f); // Return 0 if no cumulative risk found
    }

    public float calculateTradeRisk(Trades trade) {
        float entryPrice = trade.getEntryPrice();
        float stopLossPrice = trade.getStopLossPrice();
        int quantity = trade.getQuantity();
        float riskPerShare;

        if ("SELL".equalsIgnoreCase(trade.getAction())) {
            riskPerShare = entryPrice - stopLossPrice; // For short positions
        } else {
            riskPerShare = stopLossPrice - entryPrice; // For long positions
        }

        return riskPerShare * quantity;
    }




    // Monitor the risk and return a boolean indicating if the risk is within acceptable limits
    public boolean monitorRisk(float totalRisk) {
        if (totalRisk > maxDayRiskAmount) {
            logger.warn("Total risk exposure exceeds the maximum daily risk amount of ${}", maxDayRiskAmount);
            takeCorrectiveActions();
            return false;
        } else {
            logger.info("Total risk exposure is within acceptable limits: ${}", totalRisk);
            return true;
        }
    }




    private void takeCorrectiveActions() {
        try {
            // Implement corrective actions such as halting new trades or notifying users
            logger.info("Taking corrective actions due to high risk exposure.");
            // Example action: Closing all open trades
            List<Trades> openTrades = tradeRepository.findByStatusAndOrderType(TradeStatus.OPEN, DhanOrderRequest.OrderType.STOP_LOSS);
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
        //Send an email or push notification to the user
    }


    public boolean monitorCurrentDayRisk() {
        float currentDayRisk = calculateCurrentDayRisk();
        monitorRisk(currentDayRisk);
        return monitorRisk(currentDayRisk);
    }
}