package com.task.tradingAutomation.scheduler;

import com.task.tradingAutomation.service.DhanBrokerApiClient;
import com.task.tradingAutomation.service.RiskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OrderMonitoringJob {

    private static final Logger logger = LoggerFactory.getLogger(OrderMonitoringJob.class);

    @Autowired
    private DhanBrokerApiClient dhanBrokerApiClient;

    @Autowired
    private RiskManagementService riskManagementService;


    @Scheduled(fixedRateString = "${scheduler.fixedRate}") // e.g., every minute
    public void monitorOrders() {
        try {
            System.out.println("job running");
            List<Map<String, Object>> orders = dhanBrokerApiClient.fetchOrders(); //fetch all trades
            float cumulativeLoss = riskManagementService.calculateCurrentDayRisk();
            float maxDayRiskAmount = riskManagementService.getMaxDayRiskAmount();

            for (Map<String, Object> order : orders) { //in orders for stop loss orders if status is traded consider the loss
                String orderType = (String) order.get("orderType");
                String orderStatus = (String) order.get("orderStatus");
                String orderId = (String) order.get("orderId");
                float price = (float) order.get("price");
                float triggerPrice = (float) order.get("triggerPrice");
                float filledQty = (float) order.get("filled_qty");

                if ("STOP_LOSS".equalsIgnoreCase(orderType) && "TRADED".equalsIgnoreCase((String) order.get("orderStatus"))) {
                    // Calculate loss
                    float loss = calculateLoss(price, triggerPrice, filledQty);
                    cumulativeLoss += loss;

                    // Update cumulative loss
                    riskManagementService.updateRiskData(cumulativeLoss);

                    // Check if cumulative loss exceeds the maximum allowed risk
                    if (cumulativeLoss > maxDayRiskAmount) {
                        // Convert stop-loss order to market order
                        dhanBrokerApiClient.convertStopLossToMarket(order);
                        logger.info("Converted stop-loss order {} to market order due to exceeded risk.", orderId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error monitoring orders: {}", e.getMessage(), e);
        }
    }

    private float calculateLoss(float price, float triggerPrice, float filledQty) {
        return Math.abs(price - triggerPrice) * filledQty;
    }
}
