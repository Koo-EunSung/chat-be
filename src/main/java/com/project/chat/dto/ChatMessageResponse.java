package com.project.chat.dto;

import com.github.f4b6a3.tsid.Tsid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Tsid id;
    private String roomId;
    private String sender;
    private String content;
    private Instant sentAt;
}
