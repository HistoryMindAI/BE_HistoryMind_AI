package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatService Unit Tests
 * 
 * Tests for the chat service that communicates with AI backend.
 */
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

    @Test
    void processChat_WithVietnameseContent() {
        String jsonResponse = """
                    {
                        "query": "Năm 1945",
                        "intent": "year",
                        "answer": "Cách mạng tháng Tám thành công.",
                        "events": [],
                        "no_data": false
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json; charset=utf-8"));

        Mono<ChatResponse> result = chatService.processChat("Năm 1945");

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals("Năm 1945", response.getQuery());
                    assertEquals("Cách mạng tháng Tám thành công.", response.getAnswer());
                })
                .verifyComplete();
    }

    @Test
    void processChat_IdentityQuery() {
        String jsonResponse = """
                    {
                        "query": "Bạn là ai?",
                        "intent": "identity",
                        "answer": "Xin chào, tôi là History Mind AI.",
                        "events": [],
                        "no_data": false
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Mono<ChatResponse> result = chatService.processChat("Bạn là ai?");

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertEquals("identity", response.getIntent());
                    assertTrue(response.getAnswer().contains("History Mind AI"));
                })
                .verifyComplete();
    }

    @Test
    void processChat_VerifiesRequestSent() throws InterruptedException {
        String jsonResponse = """
                    {
                        "query": "test",
                        "intent": "semantic",
                        "answer": "answer",
                        "events": [],
                        "no_data": false
                    }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        chatService.processChat("test query").block();

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertTrue(recordedRequest.getBody().readUtf8().contains("test query"));
    }

    @Test
    void processChat_Timeout() {
        // Simulate a slow response
        mockWebServer.enqueue(new MockResponse()
                .setBodyDelay(10, TimeUnit.SECONDS)
                .setBody("{}"));

        Mono<ChatResponse> result = chatService.processChat("query");

        // Should timeout (default WebClient timeout)
        StepVerifier.create(result.timeout(java.time.Duration.ofSeconds(2)))
                .expectError()
                .verify();
    }

    @Test
    void processChat_MalformedJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("not valid json")
                .addHeader("Content-Type", "application/json"));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void processChat_EmptyResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .consumeNextWith(response -> {
                    assertNull(response.getQuery());
                    assertNull(response.getAnswer());
                })
                .verifyComplete();
    }

    @Test
    void processChat_ServiceUnavailable() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));

        Mono<ChatResponse> result = chatService.processChat("query");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
