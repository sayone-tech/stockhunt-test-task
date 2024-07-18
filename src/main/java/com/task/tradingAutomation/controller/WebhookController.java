package com.task.tradingAutomation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.task.tradingAutomation.util.PayloadValidator;
import com.task.tradingAutomation.dto.TradingAlert;
import com.task.tradingAutomation.service.WebhookListenerService;

@RestController
@RequestMapping("/webhook")
public class WebhookController {


    @Autowired
    private WebhookListenerService webhookListenerService;

    @Autowired
    TradingAlert tradingAlert;

    @PostMapping
    public ResponseEntity<String> receiveAlert(@RequestBody TradingAlert tradingAlert) {
        try {
            webhookListenerService.processAlert(tradingAlert);
            return new ResponseEntity<>("Alert received", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
