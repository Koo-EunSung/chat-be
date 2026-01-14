package com.project.chat;

import com.github.f4b6a3.tsid.TsidCreator;
import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.dto.ChatMessageSendRequest;
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
import static org.mockito.Mockito.*;

@ExtendWith(TestRedisConfig.class)
@SpringBootTest
@Testcontainers
public class RedisPubSubIntegrationTest {

    private static final String STOMP_DESTINATION_PREFIX = "/sub/chat/room/";
    private static final String REDIS_CHANNEL_PREFIX = "chat/room/";
    private static final int VERIFY_TIMEOUT = 5000; // ms

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @DisplayName("Redis로 메시지를 발행하면 Subscriber가 수신해서 STOMP로 전송한다.")
    @Test
    void redisPubSub() throws Exception {
        final String ROOM_ID = "1";
        final String USER = "user";
        final String CONTENT = "Test";

        ChatMessageSendRequest message = new ChatMessageSendRequest(ROOM_ID, USER, CONTENT);

        ArgumentCaptor<ChatMessageResponse> captor = ArgumentCaptor.forClass(ChatMessageResponse.class);

        redisPublisher.publish(ROOM_ID, message);

        verify(messagingTemplate, timeout(VERIFY_TIMEOUT).times(1))
                .convertAndSend(eq(STOMP_DESTINATION_PREFIX + ROOM_ID), captor.capture());

        ChatMessageResponse response = captor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(response.getSender()).isEqualTo(USER);
        assertThat(response.getContent()).isEqualTo(CONTENT);
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getSentAt()).isNotNull();
    }

    @DisplayName("다른 방의 메시지는 전파되지 않는다")
    @Test
    void roomIsolation() {
        final String ROOM_A = "A";
        final String ROOM_B = "B";

        ChatMessageSendRequest message = new ChatMessageSendRequest(ROOM_A, "USER_A", "Hello");

        redisPublisher.publish(ROOM_A, message);

        verify(messagingTemplate, timeout(VERIFY_TIMEOUT).times(1))
                .convertAndSend(
                        (String) eq(STOMP_DESTINATION_PREFIX + ROOM_A),
                        (Object) any()
                );

        verify(messagingTemplate, never())
                .convertAndSend(
                        (String) eq(STOMP_DESTINATION_PREFIX + ROOM_B),
                        (Object) any()
                );

        verifyNoMoreInteractions(messagingTemplate);
    }

    @DisplayName("Subscriber가 Redis 채널 메시지를 수신하면 WebSocket으로 전파한다.")
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

    @DisplayName("서로 다른 방 메시지가 각각 올바른 목적지로 전파된다")
    @Test
    void multiRoomMultiMessagePublish() {
        String ROOM_A = "A";
        String ROOM_B = "B";

        ArgumentCaptor<ChatMessageResponse> captorA = ArgumentCaptor.forClass(ChatMessageResponse.class);
        ArgumentCaptor<ChatMessageResponse> captorB = ArgumentCaptor.forClass(ChatMessageResponse.class);

        redisPublisher.publish(ROOM_A, new ChatMessageSendRequest(ROOM_A, "userA", "msg1"));
        redisPublisher.publish(ROOM_B, new ChatMessageSendRequest(ROOM_B, "userB", "msg2"));

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(eq(STOMP_DESTINATION_PREFIX + ROOM_A), captorA.capture());

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(eq(STOMP_DESTINATION_PREFIX + ROOM_B), captorB.capture());

        assertThat(captorA.getValue().getRoomId()).isEqualTo(ROOM_A);
        assertThat(captorB.getValue().getRoomId()).isEqualTo(ROOM_B);
    }
}
