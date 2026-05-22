package com.project.chat;

import com.project.chat.dto.ChatMessagePayload;
import com.project.chat.dto.ChatMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisStreamPublisher {
    private final StringRedisTemplate redisTemplate;

    public void publish(String roomId, ChatMessageSendRequest message) {
        ChatMessagePayload messagePayload = ChatMessagePayload.from(message);

        Map<String, String> entry = Map.of(
                "id", messagePayload.getId(),
                "roomId", messagePayload.getRoomId(),
                "sender", messagePayload.getSender(),
                "content", messagePayload.getContent(),
                "sentAt", messagePayload.getSentAt().toString()
        );

        redisTemplate.opsForStream().add(
                StreamRecords.mapBacked(entry)
                             .withStreamKey("stream:chat:room:" + roomId),
                RedisStreamCommands.XAddOptions.maxlen(1000).approximateTrimming(true)
        );
    }
}
