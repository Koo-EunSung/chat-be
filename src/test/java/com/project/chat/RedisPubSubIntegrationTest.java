package com.project.chat;

import com.github.f4b6a3.tsid.TsidCreator;
import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.dto.ChatMessageSendRequest;
import com.project.chat.event.ChatMessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(TestRedisConfig.class)
@SpringBootTest
@Testcontainers
public class RedisPubSubIntegrationTest {
    private static final int PORT = 6379;

    @Autowired
    private RedisPublisher redisPublisher;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @DisplayName("Redis로 메시지를 발행하면 Subscriber가 수신해서 STOMP로 전송한다.")
    @Test
    void redisPubSub() throws Exception {
        final String ROOM_ID = "1";
        final String USER = "user";
        final String CONTENT = "Test";
        ChatMessageSendRequest message = new ChatMessageSendRequest(ROOM_ID, USER, CONTENT);

        redisPublisher.publish(ROOM_ID, message);

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        (String) eq("/sub/chat/room/" + ROOM_ID),
                        (Object) argThat(msg -> msg instanceof ChatMessageResponse &&
                                ((ChatMessageResponse) msg).getRoomId().equals(ROOM_ID) &&
                                ((ChatMessageResponse) msg).getSender().equals(USER) &&
                                ((ChatMessageResponse) msg).getContent().equals(CONTENT)));
    }

    @DisplayName("다른 방의 메시지는 전파되지 않는다")
    @Test
    void roomIsolation() {
        final String ROOM_A = "A";
        final String ROOM_B = "B";

        ChatMessageSendRequest message = new ChatMessageSendRequest(ROOM_A, "USER_A", "Hello");

        redisPublisher.publish(ROOM_A, message);

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        (String) eq("/sub/chat/room/" + ROOM_A),
                        (Object) any()
                );

        verify(messagingTemplate, never())
                .convertAndSend(
                        (String) eq("/sub/chat/room/" + ROOM_B),
                        (Object) any()
                );
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @DisplayName("Subscriber가 Redis 채널 메시지를 수신하면 WebSocket으로 전파한다.")
    @Test
    void redisSub() {
        final String ID = TsidCreator.getTsid().toString();
        final String ROOM_ID = "1";
        final String USER = "User";
        final String CONTENT = "Hello";
        final Instant SENT_AT = Instant.now();

        ChatMessageEvent messageEvent = new ChatMessageEvent(ID, ROOM_ID, USER, CONTENT, SENT_AT);

        redisTemplate.convertAndSend("chat/room/" + ROOM_ID, messageEvent);

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        (String) eq("/sub/chat/room/" + ROOM_ID),
                        (Object) argThat(message -> message instanceof ChatMessageResponse &&
                                ((ChatMessageResponse) message).getId().equals(ID) &&
                                ((ChatMessageResponse) message).getSender().equals(USER) &&
                                ((ChatMessageResponse) message).getContent().equals(CONTENT) &&
                                ((ChatMessageResponse) message).getSentAt().equals(SENT_AT))
                );
    }

    @DisplayName("서로 다른 방 메시지가 각각 올바른 목적지로 전파된다")
    @Test
    void multiRoomMultiMessagePublish() {
        String ROOM_A = "A";
        String ROOM_B = "B";

        redisPublisher.publish(ROOM_A, new ChatMessageSendRequest(ROOM_A, "userA", "msg1"));
        redisPublisher.publish(ROOM_B, new ChatMessageSendRequest(ROOM_B, "userB", "msg2"));

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        eq("/sub/chat/room/" + ROOM_A),
                        (Object) any()
                );

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        eq("/sub/chat/room/" + ROOM_B),
                        (Object) any()
                );
    }
}
