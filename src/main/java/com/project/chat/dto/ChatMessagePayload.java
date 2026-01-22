package com.project.chat.dto;

import com.github.f4b6a3.tsid.TsidCreator;
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
public class ChatMessagePayload {
    private String id;
    private String roomId;
    private String sender;
    private String content;
    private Instant sentAt;

    public static ChatMessagePayload from(ChatMessageSendRequest request) {
        return new ChatMessagePayload(
                TsidCreator.getTsid().toString(),
                request.getRoomId(),
                request.getSender(),
                request.getContent(),
                Instant.now()
        );
    }
}
