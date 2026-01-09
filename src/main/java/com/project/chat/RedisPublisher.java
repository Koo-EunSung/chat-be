package com.project.chat;

import com.project.chat.dto.ChatMessageSendRequest;
import com.project.chat.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String roomId, ChatMessageSendRequest message) {
        ChatMessageEvent event = ChatMessageEvent.from(message);

        redisTemplate.convertAndSend("chat/room/" + roomId, event);
    }
}
