package com.task.tradingAutomation.service;

import com.task.tradingAutomation.dto.TradingAlert;
import com.task.tradingAutomation.exception.UserDefinedExceptions;
import com.task.tradingAutomation.util.PayloadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebhookListenerService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookListenerService.class);

    @Autowired
    private OrderExecutionService orderExecutionService;

    public void processAlert(TradingAlert tradingAlert) {
        // Step 1: Payload Validation
        if (!PayloadValidator.isValid(tradingAlert)) {
            throw new UserDefinedExceptions.InvalidPayloadException("Invalid payload");
        }

        try {
            orderExecutionService.executeOrder(tradingAlert);
        } catch (Exception e) {
            logger.error("Error executing order: " + e.getMessage());
            System.err.println("Error executing order: " + e.getMessage());
            throw e;
        }
    }
}