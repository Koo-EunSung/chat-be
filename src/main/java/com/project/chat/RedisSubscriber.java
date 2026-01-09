package com.project.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageEvent event = objectMapper.readValue(message.getBody(), ChatMessageEvent.class);
            ChatMessageResponse response = new ChatMessageResponse(
                    event.getId(),
                    event.getRoomId(),
                    event.getSender(),
                    event.getContent(),
                    event.getSentAt()
            );
            messagingTemplate.convertAndSend("/topic/chat", response);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
