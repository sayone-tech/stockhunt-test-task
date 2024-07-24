package com.task.tradingAutomation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.dto.*;
import com.task.tradingAutomation.enums.TradeStatus;
import com.task.tradingAutomation.repository.TradeRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;


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

    @Value("${dhan.api.access-token}")
    private String accessToken;

    @Autowired
    TradeRepository tradeRepository;


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
//        orderRequest.setPrice(newTrade.getEntryPrice()); //Price at which order is placed not considering as of now
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

    // Method to get a single trade
    public Map<String,Object> getTradeOrder(String orderId) throws JsonProcessingException {

        String url = baseUrl + "/trades/" +orderId;
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
            return responseMap;
        } else {
            logger.info("Failed to place order: " + response.getStatusCode() + " - " + response.getBody());
            responseMap.put("tradedPrice", "unknown");  // Replace with actual failure details if available
            return responseMap;
        }

    }

    public List<Map<String, Object>> fetchOrders() {
        String url = baseUrl + "/orders";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("access-token", accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers); // No request body needed for GET
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error occurred while fetching orders: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
            return Collections.emptyList(); // Return empty list on error
        } catch (Exception e) {
            logger.error("Unexpected error occurred while fetching orders: " + e.getMessage(), e);
            return Collections.emptyList(); // Return empty list on unexpected error
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseBody;

        try {
            responseBody = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON response: " + e.getMessage(), e);
            return Collections.emptyList(); // Return empty list if JSON processing fails
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Orders fetched successfully.");

            // Directly add the orders to the responseList
            if (responseBody.isArray()) {
                for (JsonNode orderNode : responseBody) {
                    responseList.add(objectMapper.convertValue(orderNode, new TypeReference<Map<String, Object>>() {}));
                }
            }
        } else {
            logger.error("Failed to fetch orders: " + response.getStatusCode() + " - " + response.getBody());
            // Return empty list on failure
            return Collections.emptyList();
        }

        return responseList;
    }


    public void convertStopLossToMarket(Map<String, Object> order) {
        try {
            // Fetch the current market price for the security
            float marketPrice = getCurrentMarketPrice(order.get("securityId").toString());

            // Find the trade by orderId from database
            Trades newTrade = tradeRepository.findByOrderId(order.get("orderId").toString());

            // Update trade details
            newTrade.setOrderType(DhanOrderRequest.OrderType.MARKET.toString());
            newTrade.setAction(DhanOrderRequest.TransactionType.SELL.toString());

            // Place the trade order to dhan api to sell
            Map<String, Object> response = placeTradeOrder(newTrade);

            //if order executed square of position and save
            if ("Success".equals(response.get("status"))) {
                logger.info("Trade order placed successfully. Order ID: " + response.get("orderId"));
                newTrade.setStatus(TradeStatus.CLOSE);
                tradeRepository.save(newTrade);
            } else {
                logger.error("Failed to place trade order. Order ID: " + response.get("orderId"));
            }
        } catch (Exception e) {
            logger.error("Error converting stop-loss to market: " + e.getMessage(), e);
        }
    }

}
