package com.task.tradingAutomation.service;

import com.task.tradingAutomation.dto.OrderRequest;
import com.task.tradingAutomation.dto.TradingAlert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class DhanBrokerApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "YOUR_DHAN_API_KEY";
    private final String baseUrl = "https://api.dhan.coY";

//    @Value("${dhan.api.key}")
//    private String apiKey;

//    @Value("${dhan.api.base-url}")
//    private String baseUrl;

    public void placeBuyOrder(TradingAlert tradingAlert) {
        placeOrder(tradingAlert, "buy");
    }

    public void placeSellOrder(TradingAlert tradingAlert) {
        placeOrder(tradingAlert, "sell");
    }

    private void placeOrder(TradingAlert tradingAlert, String action) {
        String url = baseUrl + "/orders/place";

        // Create the request body
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setSymbol(tradingAlert.getSymbolId());
        orderRequest.setQuantity(tradingAlert.getQuantity());
        orderRequest.setAction(action);
        orderRequest.setOrderType(tradingAlert.getOrderType()); // Assuming market order for simplicity
        orderRequest.setPrice(tradingAlert.getOrderType().equals("market") ? 0 : calculateStopLossPrice(tradingAlert)); // Market orders typically have no price

        // Implement methods to interact with Dhan Broker API

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Create the HTTP entity with headers and body
        HttpEntity<OrderRequest> requestEntity = new HttpEntity<>(orderRequest, headers);

        // Send the POST request
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);


//        // Send the request to Dhan Broker API
//        WebClient client = WebClient.builder()
//                .baseUrl(baseUrl)
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultHeader("Authorization", "Bearer " + apiKey)
//                .build();
//
//        WebClient.ResponseSpec responseSpec = client.post()
//                .bodyValue(orderRequest)
//                .retrieve();

        // Handle response

        // Handle the response
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Order placed successfully: " + response.getBody());
        } else {
            System.err.println("Failed to place order: " + response.getStatusCode() + " - " + response.getBody());
        }

    }

    private double calculateStopLossPrice(TradingAlert tradingAlert) {
        // This is a placeholder implementation
        double entryPrice = 100.0; // Assume an entry price (this should come from somewhere relevant)??
        double slPercent = tradingAlert.getSlPerTrade() / 100;
        if (tradingAlert.getAction().equalsIgnoreCase("buy")) {
            return entryPrice * (1 - slPercent);
        } else {
            return entryPrice * (1 + slPercent);
        }
    }

}