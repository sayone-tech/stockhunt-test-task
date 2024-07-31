package com.task.tradingAutomation.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MarketDataService implements WebSocketHandler {


    @Autowired
    WebSocketClientService webSocketClient;

    private final ConcurrentMap<String, Float> marketDataMap = new ConcurrentHashMap<>(); //to store and retrieve market data in a thread-safe manner.
    private final WebSocketService webSocketService;

    public MarketDataService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @PostConstruct
    public void init() {
        webSocketService.connect();
    }

    @Override
    public void handleMarketDataMessage(ByteBuffer message) {
        // Example: parse ticker data
        if (message.get(0) == 2) {
            int securityId = message.getInt(5);
            float lastTradedPrice = message.getFloat(9);
            marketDataMap.put(String.valueOf(securityId), lastTradedPrice);
        }
    }

    public float getCurrentMarketPrice(String exchangeSegment, String securityId) {
        // Logic to subscribe to the instrument
        webSocketService.subscribeToInstrument(exchangeSegment, securityId);

        // Wait for a short time to ensure we receive the market data
        try {
            Thread.sleep(1000); // 1s,Adjust sleep time as necessary
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get the current market value
        return marketDataMap.getOrDefault(securityId, 0.0f);
    }
}
