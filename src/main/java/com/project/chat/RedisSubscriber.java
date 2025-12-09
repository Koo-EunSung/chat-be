package com.project.chat;

import com.project.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final SimpMessageSendingOperations messagingTemplate;

    public void onMessage(ChatMessageDTO chatMessageDTO) {
        try {
            messagingTemplate.convertAndSend("/topic/chat", chatMessageDTO);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
