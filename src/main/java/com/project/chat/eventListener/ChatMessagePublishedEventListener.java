package com.project.chat.eventListener;

import com.project.chat.event.ChatMessagePublishedEvent;
import com.project.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagePublishedEventListener {

    private final ChatMessageService service;

    @EventListener
    public void saveMessage(ChatMessagePublishedEvent event) {
        service.saveAsync(event.getChatMessagePayload());
    }
}
