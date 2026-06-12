package com.epit.admin.global.websocket;

import com.epit.admin.global.config.RedisConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);

        try {
            BroadcastMessage message = new BroadcastMessage(topic, objectMapper.writeValueAsString(payload));
            redisTemplate.convertAndSend(RedisConfig.WS_BROADCAST_CHANNEL,
                    objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            log.error("Failed to publish to Redis: {}", e.getMessage());
        }
    }

    public record BroadcastMessage(String topic, String payload) {}
}
