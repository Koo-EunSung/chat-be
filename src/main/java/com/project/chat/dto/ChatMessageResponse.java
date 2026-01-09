package com.project.chat.dto;

import com.github.f4b6a3.tsid.Tsid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Tsid id;
    private String roomId;
    private String sender;
    private String content;
    private Instant sentAt;
}
