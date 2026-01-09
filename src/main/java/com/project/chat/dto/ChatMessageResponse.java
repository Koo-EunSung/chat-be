package com.project.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
    private String id;
    private String roomId;
    private String sender;
    private String content;
    private Instant sentAt;
}
