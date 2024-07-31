package com.task.tradingAutomation.service;

import java.nio.ByteBuffer;

public interface WebSocketHandler {

    void handleMarketDataMessage(ByteBuffer message);

}
