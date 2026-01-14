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

    private static final String SUBSCRIBE_PREFIX = "/sub/chat/room/";
    private static final String PUBLISH_PREFIX = "/pub/chat/room/";

    private WebSocketStompClient createClient() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(messageConverter);

        return client;
    }

    private StompSession connect(WebSocketStompClient client) throws Exception {
        return client.connectAsync(url, new StompSessionHandlerAdapter() {
                     })
                     .get(1, TimeUnit.SECONDS);
    }

    @BeforeEach
    void setUp() {
        url = "ws://localhost:" + port + "/ws-stomp/websocket"; // withSockJS() 사용 시 websocket 명시 필요
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @DisplayName("메시지를 보내면 서버에서 해당 메시지를 그대로 보낸다.")
    @Test
    void sendMessageAndReceiveEcho() throws Exception {
        String ROOM_ID = "1";

        // 1. WebSocketStompClient 생성 및 설정
        WebSocketStompClient stompClient = createClient();

        // 2. STOMP 서버에 연결
        StompSession session = connect(stompClient);
        assertThat(session).isNotNull();

        // 3. 메시지 구독
        session.subscribe(SUBSCRIBE_PREFIX + ROOM_ID, new DefaultStompFrameHandler(new ChatMessageResponse(), blockingQueue));

        // 4. 메시지 발행
        ChatMessageSendRequest sentMessage = new ChatMessageSendRequest(ROOM_ID, "Test User1", "Hello, STOMP");
        session.send(PUBLISH_PREFIX + ROOM_ID, sentMessage);

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

    @DisplayName("같은 방의 모든 클라이언트가 메시지를 수신한다")
    @Test
    void broadcastInSameRoom() throws Exception {
        String ROOM_ID = "1";
        String USER = "user";
        String CONTENT = "content";

        // 클라이언트 생성 및 설정
        WebSocketStompClient client1 = createClient();

        WebSocketStompClient client2 = createClient();

        // 서버에 연결
        StompSession session1 = connect(client1);
        StompSession session2 = connect(client2);

        BlockingQueue<ChatMessageResponse> client1Queue = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageResponse> client2Queue = new LinkedBlockingQueue<>();

        // 구독
        session1.subscribe(SUBSCRIBE_PREFIX + ROOM_ID, new DefaultStompFrameHandler(new ChatMessageResponse(), client1Queue));
        session2.subscribe(SUBSCRIBE_PREFIX + ROOM_ID, new DefaultStompFrameHandler(new ChatMessageResponse(), client2Queue));

        // 발행
        session1.send(PUBLISH_PREFIX + ROOM_ID, new ChatMessageSendRequest(ROOM_ID, USER, CONTENT));

        // 응답 수신
        ChatMessageResponse client1received = client1Queue.poll(3, TimeUnit.SECONDS);
        ChatMessageResponse client2received = client2Queue.poll(3, TimeUnit.SECONDS);

        // 검증
        assertThat(client1received).isNotNull();
        assertThat(client2received).isNotNull();

        assertThat(client1received).usingRecursiveComparison().isEqualTo(client2received);
    }

    @DisplayName("서로 다른 방의 구독자는 메시지를 받지 않는다")
    @Test
    void roomIsolation() throws Exception {
        String ROOM_ID_1 = "1";
        String ROOM_ID_2 = "2";

        WebSocketStompClient stompClient = createClient();

        StompSession session = connect(stompClient);

        BlockingQueue<ChatMessageResponse> room1Queue = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessageResponse> room2Queue = new LinkedBlockingQueue<>();

        session.subscribe(SUBSCRIBE_PREFIX + ROOM_ID_1, new DefaultStompFrameHandler(new ChatMessageResponse(), room1Queue));
        session.subscribe(SUBSCRIBE_PREFIX + ROOM_ID_2, new DefaultStompFrameHandler(new ChatMessageResponse(), room2Queue));

        session.send(PUBLISH_PREFIX + ROOM_ID_1, new ChatMessageSendRequest(ROOM_ID_1, "user", "hello"));

        assertThat(room1Queue.poll(3, TimeUnit.SECONDS)).isNotNull();
        assertThat(room2Queue.poll(1, TimeUnit.SECONDS)).isNull();
    }
}
