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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
}
