package com.project.chat;

import com.project.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void onMessage(String publishMessage) {
        try {
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(publishMessage, ChatMessageDTO.class);
            messagingTemplate.convertAndSend("/topic/chat", chatMessageDTO);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
