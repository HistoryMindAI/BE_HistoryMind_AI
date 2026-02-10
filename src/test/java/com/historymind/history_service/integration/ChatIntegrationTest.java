package com.historymind.history_service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("ai.service.url", () -> mockWebServer.url("/").toString());
    }

    @Test
    void testEndToEndFlow() throws JsonProcessingException {
        // Mock AI Response (snake_case from Python)
        String aiResponseJson = """
            {
                "query": "Who is Uncle Ho?",
                "intent": "person_query",
                "answer": "Ho Chi Minh was a revolutionary leader.",
                "events": [],
                "no_data": false
            }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(aiResponseJson)
                .addHeader("Content-Type", "application/json"));

        // Frontend Request
        ChatRequest request = new ChatRequest("Who is Uncle Ho?");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatResponse.class)
                .consumeWith(result -> {
                    ChatResponse response = result.getResponseBody();
                    assertEquals("Who is Uncle Ho?", response.getQuery());
                    assertEquals("person_query", response.getIntent());
                    assertEquals("Ho Chi Minh was a revolutionary leader.", response.getAnswer());
                    assertEquals(false, response.isNoData());
                });
    }
}
