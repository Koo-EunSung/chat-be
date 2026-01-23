package com.project.chat.service;

import com.project.chat.dto.ChatMessagePayload;
import com.project.chat.entity.ChatMessage;
import com.project.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;

    @Async
    public void saveAsync(ChatMessagePayload payload) {
        repository.insert(new ChatMessage(
                payload.getId(),
                payload.getRoomId(),
                payload.getSender(),
                payload.getContent(),
                payload.getSentAt())
        );
    }
}
