package com.project.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @MessageMapping("/send")
    @SendTo("/topic/chat")
    public ChatMessageDTO send(ChatMessageDTO message) {
        System.out.println("Received: " + message.getContent());
        return message;
    }
}
