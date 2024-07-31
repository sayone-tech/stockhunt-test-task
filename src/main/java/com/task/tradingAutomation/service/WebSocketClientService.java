package com.task.tradingAutomation.service;

import jakarta.websocket.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.ByteBuffer;

@Component
@ClientEndpoint
public class WebSocketClientService {

    @Value("${dhan.api.access-token}")
    private String apiAccessToken;

    @Value("${dhan.api.id}")
    private String clientId;

    private Session session;
    private final WebSocketHandler webSocketHandler;

    public WebSocketClientService(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }


    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        ByteBuffer authPacket = createAuthorizationPacket();
        session.getAsyncRemote().sendBinary(authPacket);
    }

    @OnMessage
    public void onMessage(ByteBuffer message) { //market data updates asynchronously through the onMessage method after subscription
        webSocketHandler.handleMarketDataMessage(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket closed: " + closeReason);
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("wss://api-feed.dhan.co"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribeToInstrument(String exchangeSegment, String securityId) { //Once subscribed, the server starts sending market data updates for the subscribed instrument
        if (session != null && session.isOpen()) {
            ByteBuffer subscribePacket = createSubscribePacket(exchangeSegment, securityId);
            session.getAsyncRemote().sendBinary(subscribePacket);
        }
    }

    private ByteBuffer createAuthorizationPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(584); // 83 header + 500 API token + 1 byte padding
        buffer.put((byte) 11); // Feed Request Code for new feed
        buffer.putShort((short) 583); // Message Length
        buffer.put(clientId.getBytes()); // Client ID
        buffer.position(34);
        buffer.put(new byte[50]); // Dhan Auth
        buffer.put(apiAccessToken.getBytes()); // API Access Token
        buffer.put((byte) 2); // Authentication Type
        return buffer;
    }

    private ByteBuffer createSubscribePacket(String exchangeSegment, String securityId) {
        ByteBuffer buffer = ByteBuffer.allocate(2187); // 83 header + 4 instrument count + 2100 instrument data
        buffer.put(createAuthorizationPacket());
        buffer.putInt(1); // Number of Instruments to subscribe
        buffer.put(exchangeSegment.getBytes());
        buffer.put(securityId.getBytes());
        return buffer;
    }

}
