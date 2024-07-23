package com.task.tradingAutomation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.dto.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public Map<String,Object> placeTradeOrder(Trades newTrade) throws JsonProcessingException {
        String url = baseUrl + "/orders";
        // Create the request body
        DhanOrderRequest orderRequest = new DhanOrderRequest();
        orderRequest.setDhanClientId(userId);
        orderRequest.setTransactionType(DhanOrderRequest.TransactionType.valueOf(newTrade.getAction().toUpperCase()));
        orderRequest.setExchangeSegment(DhanOrderRequest.ExchangeSegment.NSE_EQ);
        orderRequest.setProductType(DhanOrderRequest.ProductType.INTRADAY);
        orderRequest.setOrderType(DhanOrderRequest.OrderType.valueOf(newTrade.getOrderType().toUpperCase()));
        orderRequest.setValidity(DhanOrderRequest.Validity.DAY);
        orderRequest.setSecurityId(newTrade.getSymbolId());
        orderRequest.setQuantity(newTrade.getQuantity());
        orderRequest.setPrice(newTrade.getEntryPrice()); //Price at which order is placed
        orderRequest.setTriggerPrice(newTrade.getStopLossPrice());

        // to interact with Dhan Broker API-

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<DhanOrderRequest> requestEntity = new HttpEntity<>(orderRequest, headers);// Create the HTTP entity with headers and body
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class); // Send the POST request

        // Prepare the response map
        Map<String, Object> responseMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        // Handle the response
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Order placed successfully: " + response.getBody());
            responseMap.put("orderId",responseBody.path("orderId").asText());
            responseMap.put("orderStatus",responseBody.path("orderStatus").asText());
            responseMap.put("status","Success");
            return responseMap;
        } else {
            logger.info("Failed to place order: " + response.getStatusCode() + " - " + response.getBody());
            responseMap.put("orderId", "unknown");  // Replace with actual failure details if available
            responseMap.put("orderStatus", "unknown");
            responseMap.put("status","Failed");
            return responseMap;
        }

    }


    public float getCurrentMarketPrice(String symbolId) {
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

//    public float placeBuyOrSellOrder(OrderAlert alert) throws Exception {
//
//        String url = baseUrl + "/orders";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + apiKey);
//        headers.set("Content-Type", "application/json");
//
//        // Construct the request body
//
//        DhanOrderRequest orderRequest = new DhanOrderRequest();
//        orderRequest.setDhanClientId(userId);
//        orderRequest.setTransactionType(DhanOrderRequest.TransactionType.BUY);
//        orderRequest.setSecurityId(alert.getSymbolId());
//        orderRequest.setQuantity(alert.getQuantity());
//        orderRequest.setPrice(alert.getPrice()); //Price at which order is placed
//
//        HttpEntity<DhanOrderRequest> requestEntity = new HttpEntity<>(orderRequest, headers);// Create the HTTP entity with headers and body
//        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class); // Send the POST request
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode responseBody = objectMapper.readTree(response.getBody());
//
//            // Extract the filled price from the response
//            float filledPrice = responseBody.get("filledPrice").floatValue(); // Adjust based on the actual response structure
//            return filledPrice;
//        } else {
//            throw new Exception("Failed to place buy order: " + response.getStatusCode());
//        }
//    }

    public Map<String,Object> getTradeOrder(String orderId) throws JsonProcessingException {

        String url = baseUrl + "/orders/" +orderId;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<DhanOrderRequest> requestEntity = new HttpEntity<>(headers);// Create the HTTP entity with headers and body
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        // Prepare the response map
        Map<String, Object> responseMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseBody = objectMapper.readTree(response.getBody());
        // Handle the response
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Order placed successfully: " + response.getBody());
            responseMap.put("tradedPrice",responseBody.path("tradedPrice").asText());
            responseMap.put("triggerPrice",responseBody.path("triggerPrice").asText());
            return responseMap;
        } else {
            logger.info("Failed to place order: " + response.getStatusCode() + " - " + response.getBody());
            responseMap.put("tradedPrice", "unknown");  // Replace with actual failure details if available
            return responseMap;
        }

    }
    }
