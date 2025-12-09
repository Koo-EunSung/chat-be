package com.project.chat.controller;

import com.project.chat.RedisPublisher;
import com.project.chat.dto.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final RedisPublisher redisPublisher;
    private final ChannelTopic channelTopic;

    @MessageMapping("/send")
    public void message(ChatMessageDTO message) {
        redisPublisher.publish(channelTopic, message);
    }
}
