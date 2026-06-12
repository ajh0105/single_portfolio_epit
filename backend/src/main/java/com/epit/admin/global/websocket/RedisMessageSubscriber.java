package com.epit.admin.global.websocket;

import com.epit.admin.global.websocket.WebSocketEventPublisher.BroadcastMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public void onMessage(String message, String pattern) {
        try {
            BroadcastMessage broadcast = objectMapper.readValue(message, BroadcastMessage.class);
            // Redis에서 수신한 메시지를 이 인스턴스의 WebSocket 구독자에게 전달
            messagingTemplate.convertAndSend(broadcast.topic(), broadcast.payload());
        } catch (Exception e) {
            log.error("Failed to process Redis broadcast message: {}", e.getMessage());
        }
    }
}
