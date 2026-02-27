package com.project.chat;

import com.project.chat.dto.ChatMessageSendRequest;
import com.project.chat.entity.ChatMessage;
import com.project.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(TestRedisConfig.class)
@SpringBootTest
@Testcontainers
public class ChatMessageTest {

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
    }

    @DisplayName("채팅 메시지를 발행하면 MongoDB에 저장된다")
    @Test
    void whenPublish_thenMessageIsSaved() {
        // given
        final String ROOM_ID = "room1";
        final String SENDER = "sender1";
        final String CONTENT = "Hello, world!";
        ChatMessageSendRequest request = new ChatMessageSendRequest(ROOM_ID, SENDER, CONTENT);

        // when
        redisPublisher.publish(ROOM_ID, request);

        // then
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(chatMessageRepository.count()).isEqualTo(1);
                });

        await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ChatMessage saved = chatMessageRepository.findAll().get(0);

                    assertThat(saved.getRoomId()).isEqualTo(ROOM_ID);
                    assertThat(saved.getSender()).isEqualTo(SENDER);
                    assertThat(saved.getContent()).isEqualTo(CONTENT);
                });
    }
}
