package com.project.chat.controller;

import com.project.chat.dto.ChatMessageSendRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat/room/{roomId}")
    public void message(@DestinationVariable String roomId, ChatMessageSendRequest message) {
    }
}
