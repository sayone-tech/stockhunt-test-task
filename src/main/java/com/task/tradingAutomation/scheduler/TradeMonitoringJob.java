package com.task.tradingAutomation.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.task.tradingAutomation.dto.DhanOrderRequest;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.enums.TradeStatus;
import com.task.tradingAutomation.repository.TradeRepository;
import com.task.tradingAutomation.service.DhanBrokerApiClient;
import com.task.tradingAutomation.service.OrderExecutionService;
import com.task.tradingAutomation.service.RiskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
    public void monitorTrades() throws JsonProcessingException {
        System.out.println("Task is running");
        List<Trades> openTrades = tradeRepository.findByStatus(TradeStatus.OPEN);
        double currentTotalRisk = 0.0;
        for (Trades trade : openTrades) {
            switch (trade.getOrderStatus()) {
                case "TRADED":
                    Map<String, Object> response = dhanBrokerApiClient.getTradeOrder(trade.getOrderId());
                    float triggeredPrice = (float) response.get("triggerPrice"); // Price at which the order is triggered

                    if (trade.getStopLossPrice() == triggeredPrice) {
                        // No action needed if stop loss price matches the triggered price
                        continue;
                    }

                    if (trade.getStopLossPrice() > triggeredPrice && trade.getAction().equals(DhanOrderRequest.TransactionType.BUY.toString())) {
                        // If stop loss price is higher and action is BUY, change action to SELL
                        trade.setAction(DhanOrderRequest.TransactionType.SELL.toString());
                        trade.setOrderType(DhanOrderRequest.OrderType.MARKET.toString());
                        dhanBrokerApiClient.placeTradeOrder(trade);
                    } else if (trade.getStopLossPrice() < triggeredPrice && trade.getAction().equals(DhanOrderRequest.TransactionType.SELL.toString())) {
                        // If stop loss price is lower and action is SELL, change action to BUY
                        trade.setAction(DhanOrderRequest.TransactionType.BUY.toString());
                        trade.setOrderType(DhanOrderRequest.OrderType.MARKET.toString());
                        dhanBrokerApiClient.placeTradeOrder(trade);
                    }

                    // Calculate the risk for the current trade
                    double tradeRisk = riskManagementService.calculateTradeRisk(trade);
                    currentTotalRisk += tradeRisk;
                    break;

                case "TRANSIT":
                    // Handle cancelled trades, maybe log or update the database
                    System.out.println("Trade has not reach the exchange server: " + trade.getOrderId());
                    break;

                case "PENDING":
                    // Handle cancelled trades, maybe log or update the database
                    System.out.println("Trade has reached at exchange end, awaiting execution: " + trade.getOrderId());
                    break;

                case "REJECTED":
                    // Handle pending trades
                    System.out.println("Trade is rejected at exchange/broker’s end: " + trade.getOrderId());
                    break;

                case "CANCELLED":
                    // Handle partially filled trades
                    System.out.println("Trade has been cancelled by user: " + trade.getOrderId());
                    break;

                case "EXPIRED":
                    // Handle rejected trades
                    System.out.println("Validity of order is expired: " + trade.getOrderId());
                    break;


                default:
                    // Handle unexpected statuses
                    System.out.println("Unknown trade status: " + trade.getOrderStatus());
                    break;
            }
        }
    }
}