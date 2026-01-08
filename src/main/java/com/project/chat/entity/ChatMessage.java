package com.project.chat.entity;

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
}
