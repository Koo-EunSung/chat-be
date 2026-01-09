package com.project.chat;

import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber {
    private final SimpMessageSendingOperations messagingTemplate;

    public void onMessage(ChatMessageEvent event) {
        try {
            ChatMessageResponse response = new ChatMessageResponse(
                    event.getId(),
                    event.getRoomId(),
                    event.getSender(),
                    event.getContent(),
                    event.getSentAt()
            );
            messagingTemplate.convertAndSend("/sub/chat/room/" + event.getRoomId(), response);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
