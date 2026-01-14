package com.project.chat;

import com.github.f4b6a3.tsid.TsidCreator;
import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.event.ChatMessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(TestRedisConfig.class)
@SpringBootTest
@Testcontainers
public class RedisSubscriberTest {

    private static final String STOMP_DESTINATION_PREFIX = "/sub/chat/room/";
    private static final int VERIFY_TIMEOUT = 5000;
    private static final String REDIS_CHANNEL_PREFIX = "chat/room/";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @DisplayName("Subscriber는 Redis 메시지를 STOMP 메시지로 변환한다")
    @Test
    void redisSub() {
        final String ID = TsidCreator.getTsid().toString();
        final String ROOM_ID = "1";
        final String USER = "User";
        final String CONTENT = "Hello";
        final Instant SENT_AT = Instant.now();

        ChatMessageEvent messageEvent = new ChatMessageEvent(ID, ROOM_ID, USER, CONTENT, SENT_AT);

        ArgumentCaptor<ChatMessageResponse> captor = ArgumentCaptor.forClass(ChatMessageResponse.class);

        redisTemplate.convertAndSend(REDIS_CHANNEL_PREFIX + ROOM_ID, messageEvent);

        verify(messagingTemplate, timeout(VERIFY_TIMEOUT).times(1))
                .convertAndSend(eq(STOMP_DESTINATION_PREFIX + ROOM_ID), captor.capture());

        ChatMessageResponse response = captor.getValue();

        assertThat(response.getId()).isEqualTo(ID);
        assertThat(response.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(response.getSender()).isEqualTo(USER);
        assertThat(response.getContent()).isEqualTo(CONTENT);
        assertThat(response.getSentAt()).isEqualTo(SENT_AT);
    }
}
