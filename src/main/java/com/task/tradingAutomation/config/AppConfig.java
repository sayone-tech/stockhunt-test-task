package com.task.tradingAutomation.config;

import com.task.tradingAutomation.dto.TradingAlert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public TradingAlert tradingAlert() {
        return new TradingAlert();
    }
}