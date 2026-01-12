package com.project.chat;

import com.project.chat.dto.ChatMessageResponse;
import com.project.chat.dto.ChatMessageSendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketChatTests {
    @Value("${local.server.port}")
    private int port;

    @Autowired
    private MessageConverter messageConverter; // Spring 컨텍스트에서 MessageConverter를 주입받음

    private String url;
    private BlockingQueue<ChatMessageResponse> blockingQueue;

    @BeforeEach
    void setUp() {
        url = "ws://localhost:" + port + "/ws-stomp/websocket"; // withSockJS() 사용 시 websocket 명시 필요
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @DisplayName("메시지를 보내면 서버에서 해당 메시지를 그대로 보낸다.")
    @Test
    void sendMessageAndReceiveEcho() throws Exception {
        // 1. WebSocketStompClient 생성 및 설정
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        stompClient.setMessageConverter(messageConverter);

        // 2. STOMP 서버에 연결
        StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
        }).get(1, TimeUnit.SECONDS);
        assertThat(session).isNotNull();

        // 3. 메시지 구독
        session.subscribe("/sub/chat/room/1", new DefaultStompFrameHandler(new ChatMessageResponse(), blockingQueue));

        // 4. 메시지 발행
        ChatMessageSendRequest sentMessage = new ChatMessageSendRequest("1", "Test User1", "Hello, STOMP");
        session.send("/pub/chat/room/1", sentMessage);

        // 5. 응답 수신
        ChatMessageResponse response = blockingQueue.poll(3, TimeUnit.SECONDS);

        // 6. 검증
        // assertThat(response).usingRecursiveComparison().isEqualTo(sentMessage);

        assertThat(response).isNotNull();
        assertThat(response.getSender()).isEqualTo(sentMessage.getSender());
        assertThat(response.getContent()).isEqualTo(sentMessage.getContent());

        session.disconnect();
    }

    public class DefaultStompFrameHandler<T> implements StompFrameHandler {
        private final T response;
        private final BlockingQueue<T> responses;

        public DefaultStompFrameHandler(final T response, final BlockingQueue<T> responses) {
            this.response = response;
            this.responses = responses;
        }

        @Override
        public Type getPayloadType(final StompHeaders headers) {
            return response.getClass();
        }


        @Override
        public void handleFrame(final StompHeaders headers, final Object payload) {
            responses.offer((T) payload);
        }
    }
}
