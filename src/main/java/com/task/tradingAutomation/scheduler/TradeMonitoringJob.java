package com.task.tradingAutomation.scheduler;

import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.repository.TradeRepository;
import com.task.tradingAutomation.service.DhanBrokerApiClient;
import com.task.tradingAutomation.service.OrderExecutionService;
import com.task.tradingAutomation.service.RiskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradeMonitoringJob {

    @Autowired
    RiskManagementService riskManagementService;

    @Autowired
    OrderExecutionService orderExecutionService;

    @Autowired
    DhanBrokerApiClient dhanBrokerApiClient;

    @Autowired
    TradeRepository tradeRepository;


    // Scheduled job to monitor trades every minute -Periodically checks if any open trades have hit their stop loss
    // or if cumulative risk requires closing all trades

    @Scheduled(fixedRate = 60000) // Check every minute
    public void monitorTrades() {
        System.out.println("Task is running");
        List<Trades> openTrades = tradeRepository.findByStatus("open");
        for (Trades trade : openTrades) {
            double currentPrice = dhanBrokerApiClient.getCurrentMarketPrice(trade.getSymbolId());
            if (trade.getAction().equalsIgnoreCase("buy") && currentPrice <= trade.getStopLossPrice()) {
                orderExecutionService.closeTrade(trade);
            } else if (trade.getAction().equalsIgnoreCase("sell") && currentPrice >= trade.getStopLossPrice()) {
                orderExecutionService.closeTrade(trade);
            }
        }
        if (riskManagementService.calculateCurrentDayRisk() > riskManagementService.getMaxDayRiskAmount()) {
            riskManagementService.closeOpenTrades();
        }
    }
}