package com.historymind.history_service.controller;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import com.historymind.history_service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    public void testAskHistory() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("Hello");
        mockResponse.setAnswer("Hi there");
        mockResponse.setEvents(Collections.emptyList());

        Mockito.when(chatService.processChat(anyString()))
                .thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("Hello");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatResponse.class)
                .value(response -> {
                    assert response.getAnswer().equals("Hi there");
                });
    }
}
