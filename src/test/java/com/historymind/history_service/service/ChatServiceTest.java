package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServiceTest {

    private MockWebServer mockWebServer;
    private ChatService chatService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        chatService = new ChatService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void processChat_Success() {
        String jsonResponse = """
            {
                "query": "query",
                "intent": "intent",
                "answer": "answer",
                "events": [],
                "no_data": false
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals("query", response.getQuery());
                    assertEquals("intent", response.getIntent());
                    assertEquals("answer", response.getAnswer());
                    assertFalse(response.isNoData());
                })
                .verifyComplete();
    }

    @Test
    void processChat_WithNoData() {
        String jsonResponse = """
            {
                "query": "query",
                "intent": "unknown",
                "answer": null,
                "events": [],
                "no_data": true
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertTrue(response.isNoData());
                })
                .verifyComplete();
    }

    @Test
    void processChat_Error() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
