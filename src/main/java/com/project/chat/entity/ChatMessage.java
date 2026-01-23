package com.project.chat.entity;

import com.project.chat.dto.ChatMessagePayload;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Getter
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String id;

    private String roomId;

    private String sender;

    private String content;

    private Instant sentAt;

    public static ChatMessage from(ChatMessagePayload payload) {
        return new ChatMessage(
                payload.getId(),
                payload.getRoomId(),
                payload.getSender(),
                payload.getContent(),
                payload.getSentAt()
        );
    }
}
