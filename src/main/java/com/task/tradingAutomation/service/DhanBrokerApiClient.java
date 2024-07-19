package com.task.tradingAutomation.service;

import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.dto.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
public class DhanBrokerApiClient {
    private static final Logger logger = LoggerFactory.getLogger(OrderExecutionService.class);


    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${dhan.api.key}")
    private String apiKey;

    @Value("${dhan.api.id}")
    private String userId;

    @Value("${dhan.api.url}")
    private String baseUrl;


    @RateLimiter(name = "dhanApi")
    public String placeTradeOrder(OrderAlert alert) {
        String url = baseUrl + "/orders";

        // Create the request body
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setNameOfStrategy(alert.getStrategyName());
        orderRequest.setSymbolId(alert.getSymbolId());
        orderRequest.setQuantity(alert.getQuantity());
        orderRequest.setAction(alert.getAction());
        orderRequest.setOrderType(alert.getOrderType());
        orderRequest.setPrice(alert.getStopLossPrice()); // Market orders typically have no price

        // to interact with Dhan Broker API-

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("User-Dhan-ID", userId);
        HttpEntity<OrderRequest> requestEntity = new HttpEntity<>(orderRequest, headers);// Create the HTTP entity with headers and body
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class); // Send the POST request

        // Handle the response
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Order placed successfully: " + response.getBody());
            return "Order placed successfully";
        } else {
            logger.info("Failed to place order: " + response.getStatusCode() + " - " + response.getBody());
            return "Failed to place order";
        }

    }


    public double getCurrentMarketPrice(String symbolId) {
        String url = baseUrl + "/market-price/" + symbolId;
        RestTemplate restTemplate = new RestTemplate();

        try {
            MarketPriceResponse response = restTemplate.getForObject(url, MarketPriceResponse.class);
            if (response == null ) {
                throw new RuntimeException("Failed to retrieve market price. Response is null or missing price.");
            }
            return response.getPrice();
        } catch (RestClientException e) {
            logger.error("Error fetching market price from API: " + e.getMessage());
            // Return a default value or rethrow the exception based on your application logic
            throw new RuntimeException("Error fetching market price.", e);
        }
    }


    // Method to close multiple trades at once
    public void closeAllOpenTrades(List<Trades> openTrades) {
        String endpoint = baseUrl + "/closeAllTrades"; // Adjust endpoint as necessary-get correct api
        CloseTradesRequest request = new CloseTradesRequest(openTrades);// Construct request payload
        restTemplate.postForObject(endpoint, request, Void.class);// Make the API call
    }


    // Method to close a single trade
    public void closeTrade(String symbolId, int quantity) {
        String endpoint = baseUrl + "/closeTrade"; // Adjust endpoint as necessary-get correct api
        CloseTrade request = new CloseTrade(symbolId,quantity);
        restTemplate.postForObject(endpoint, request, Void.class);//api call
    }
}