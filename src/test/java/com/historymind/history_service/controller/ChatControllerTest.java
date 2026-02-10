package com.historymind.history_service.controller;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import com.historymind.history_service.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChatService chatService;

    @Test
    public void testAskHistory() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setQuery("test query");
        mockResponse.setAnswer("test answer");
        mockResponse.setEvents(Collections.emptyList());
        mockResponse.setNoData(false);

        Mockito.when(chatService.processChat(anyString()))
                .thenReturn(Mono.just(mockResponse));

        ChatRequest request = new ChatRequest("test query");

        webTestClient.post()
                .uri("/api/v1/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.query").isEqualTo("test query")
                .jsonPath("$.answer").isEqualTo("test answer")
                .jsonPath("$.noData").isEqualTo(false);
    }
}
