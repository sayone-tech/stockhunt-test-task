package com.task.tradingAutomation.config;

import com.task.tradingAutomation.service.WebSocketService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketService webSocketService() {
        return new WebSocketService() {
            @Override
            public void connect() {

            }

            @Override
            public void subscribeToInstrument(String exchangeSegment, String securityId) {

            }
        };
    }
}