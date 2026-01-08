package com.project.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.chat.dto.ChatMessageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.mockito.Mockito.*;

@ExtendWith(TestRedisConfig.class)
@SpringBootTest
@Testcontainers
public class RedisPubSubIntegrationTest {
    private static final int PORT = 6379;

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private ChannelTopic channelTopic;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Redis로 메시지를 발행하면 Subscriber가 수신해서 STOMP로 전송한다.")
    @Test
    void redisPubSub() throws Exception {
        final String USER = "user";
        final String CONTENT = "Test";
        ChatMessageDTO message = new ChatMessageDTO(USER, CONTENT);

        redisPublisher.publish(channelTopic, message);

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        (String) eq("/topic/chat"),
                        (Object) argThat(msg -> msg instanceof ChatMessageDTO &&
                                ((ChatMessageDTO) msg).getSender().equals(USER) &&
                                ((ChatMessageDTO) msg).getContent().equals(CONTENT)));
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @DisplayName("Subscriber가 Redis 채널 메시지를 수신하면 WebSocket으로 전파한다.")
    @Test
    void redisSub() {
        String user = "User";
        String content = "Hello";
        ChatMessageDTO messageDTO = new ChatMessageDTO(user, content);

        redisTemplate.convertAndSend(channelTopic.getTopic(), messageDTO);

        verify(messagingTemplate, timeout(5000).times(1))
                .convertAndSend(
                        (String) eq("/topic/chat"),
                        (Object) argThat(message -> message instanceof ChatMessageDTO &&
                                ((ChatMessageDTO) message).getSender().equals(user) &&
                                ((ChatMessageDTO) message).getContent().equals(content)));
    }
}
