package com.project.chat.event;

import com.project.chat.dto.ChatMessagePayload;
import org.springframework.context.ApplicationEvent;

public class ChatMessagePublishedEvent extends ApplicationEvent {
    private final ChatMessagePayload chatMessagePayload;

    public ChatMessagePublishedEvent(Object source, ChatMessagePayload chatMessagePayload) {
        super(source);
        this.chatMessagePayload = chatMessagePayload;
    }

    public ChatMessagePayload getChatMessagePayload() {
        return chatMessagePayload;
    }
}
