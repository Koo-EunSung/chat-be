package com.project.chat.event;

import com.github.f4b6a3.tsid.Tsid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 백엔드 내부 전달용 객체
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent {
    private Tsid id;
    private String roomId;
    private String sender;
    private String content;
    private Instant sentAt;
}
