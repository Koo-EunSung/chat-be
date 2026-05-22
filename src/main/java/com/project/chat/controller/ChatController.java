package com.project.chat.controller;

import com.project.chat.RedisStreamPublisher;
import com.project.chat.dto.ChatMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RedisStreamPublisher streamPublisher;

    @MessageMapping("/chat/room/{roomId}")
    public void message(@DestinationVariable String roomId, ChatMessageSendRequest message) {
        streamPublisher.publish(roomId, message);
    }
}
