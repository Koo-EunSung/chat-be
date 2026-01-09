package com.project.chat;

import com.github.f4b6a3.tsid.TsidCreator;
import com.project.chat.dto.ChatMessageSendRequest;
import com.project.chat.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String roomId, ChatMessageSendRequest message) {
        ChatMessageEvent event = new ChatMessageEvent(
                TsidCreator.getTsid(),
                message.getRoomId(),
                message.getSender(),
                message.getContent(),
                Instant.now()
        );

        redisTemplate.convertAndSend("chat/room/" + roomId, event);
    }
}
