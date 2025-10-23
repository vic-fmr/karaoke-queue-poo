package com.karaoke.backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.util.ArrayList;
import java.util.List;  

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@Import(WebSocketIntegrationTest.TestSecurityConfig.class)
public class WebSocketIntegrationTest {

    @TestConfiguration
    static class TestSecurityConfig {
        
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);
            
            return http.build();
        }
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @LocalServerPort
    private int port;
    
    private String URL;
    private BlockingQueue<String> messages;

    private static final String TOPIC_DESTINATION = "/topic/queue-update"; 

    @BeforeEach
    public void setup() {
        this.URL = "ws://localhost:" + port + "/ws";
        this.messages = new LinkedBlockingQueue<>();
    }

    @WithMockUser
    @Test
    void clientSubscription_ShouldReceiveMessageWhenServerSendsToTopic() throws Exception {

        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        WebSocketClient transport = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(transport);
        stompClient.setMessageConverter(new StringMessageConverter());

        StompSession session = null;
        try {
            session = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Falha ao conectar ao endpoint Web Socket: " + e.getMessage());
            return;
        }

        session.subscribe(TOPIC_DESTINATION, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.add((String) payload);
            }
        });
        
        Thread.sleep(200); 

        messagingTemplate.convertAndSend(TOPIC_DESTINATION, "{\"status\": \"fila atualizada\", \"session\": \"123\"}");

        String receivedMessage = messages.poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedMessage, "Nenhuma mensagem Web Socket recebida no tópico " + TOPIC_DESTINATION);
        
        session.disconnect();
    }
}

