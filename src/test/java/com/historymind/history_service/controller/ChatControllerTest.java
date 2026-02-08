package com.historymind.history_service.controller;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import com.historymind.history_service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatController Integration Tests
 * 
 * Tests for the chat API endpoints including edge cases and error handling.
 */
@WebFluxTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    public void askHistory_Success() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("query");
        mockResponse.setIntent("intent");
        mockResponse.setAnswer("answer");
        mockResponse.setEvents(Collections.emptyList());
        mockResponse.setNoData(false);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("query");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.query").isEqualTo("query")
                .jsonPath("$.intent").isEqualTo("intent")
                .jsonPath("$.answer").isEqualTo("answer")
                .jsonPath("$.noData").isEqualTo(false);
    }

    @Test
    public void askHistory_EmptyResponse() {
        when(chatService.processChat(anyString())).thenReturn(Mono.empty());

        ChatRequest request = new ChatRequest("query");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void askHistory_WithVietnameseQuery() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("Năm 1945 có sự kiện gì?");
        mockResponse.setIntent("year");
        mockResponse.setAnswer("Cách mạng tháng Tám thành công.");
        mockResponse.setEvents(Collections.emptyList());
        mockResponse.setNoData(false);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("Năm 1945 có sự kiện gì?");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.query").isEqualTo("Năm 1945 có sự kiện gì?")
                .jsonPath("$.intent").isEqualTo("year");
    }

    @Test
    public void askHistory_WithNoData() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("unknown");
        mockResponse.setIntent("semantic");
        mockResponse.setAnswer(null);
        mockResponse.setEvents(Collections.emptyList());
        mockResponse.setNoData(true);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("unknown");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.noData").isEqualTo(true)
                .jsonPath("$.answer").doesNotExist();
    }

    @Test
    public void askHistory_WithEvents() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("Trận Bạch Đằng");
        mockResponse.setIntent("semantic");
        mockResponse.setAnswer("Trận Bạch Đằng năm 938.");
        mockResponse.setNoData(false);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("Trận Bạch Đằng");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.answer").isEqualTo("Trận Bạch Đằng năm 938.");
    }

    @Test
    public void askHistory_IdentityQuery() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("Bạn là ai?");
        mockResponse.setIntent("identity");
        mockResponse.setAnswer("Xin chào, tôi là History Mind AI.");
        mockResponse.setEvents(Collections.emptyList());
        mockResponse.setNoData(false);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("Bạn là ai?");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.intent").isEqualTo("identity")
                .jsonPath("$.answer").isNotEmpty();
    }

    @Test
    public void askHistory_EmptyQuery() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("");
        mockResponse.setIntent("unknown");
        mockResponse.setAnswer(null);
        mockResponse.setNoData(true);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void askHistory_LongQuery() {
        String longQuery = "A".repeat(1000);

        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery(longQuery);
        mockResponse.setNoData(true);

        when(chatService.processChat(anyString())).thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest(longQuery);

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}
