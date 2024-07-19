package com.task.tradingAutomation.controller;

import com.task.tradingAutomation.dto.TradingAlertRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.task.tradingAutomation.util.PayloadValidator;
import com.task.tradingAutomation.dto.TradingAlertRequest;
import com.task.tradingAutomation.service.WebhookListenerService;

@RestController
@RequestMapping("/webhook")
public class WebhookController {


    @Autowired
    private WebhookListenerService webhookListenerService;

    @Autowired
    TradingAlertRequest tradingAlert;

    @Value("${webhook.secret}")
    private String webhookSecret;

    @PostMapping("/alert")
    public ResponseEntity<String>receiveAlert(@RequestHeader("X-Webhook-Secret") String secret,
                                              @RequestBody TradingAlertRequest tradingAlert) {
        try {
            webhookListenerService.processAlert(tradingAlert);
            return new ResponseEntity<>("Alert received", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
