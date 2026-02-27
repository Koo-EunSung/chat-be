package com.project.chat;

import com.project.chat.dto.ChatMessagePayload;
import com.project.chat.dto.ChatMessageSendRequest;
import com.project.chat.event.ChatMessagePublishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(String roomId, ChatMessageSendRequest message) {
        ChatMessagePayload event = ChatMessagePayload.from(message);

        redisTemplate.convertAndSend("chat/room/" + roomId, event);
        eventPublisher.publishEvent(new ChatMessagePublishedEvent(this, event));
    }
}
