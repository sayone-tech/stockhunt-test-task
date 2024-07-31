package com.task.tradingAutomation.service;

public interface WebSocketService {
    void connect();
    void subscribeToInstrument(String exchangeSegment, String securityId);
}