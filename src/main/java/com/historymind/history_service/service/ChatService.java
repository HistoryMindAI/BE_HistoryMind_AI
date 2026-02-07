package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatService {

    private final WebClient webClient;

    public ChatService(WebClient aiWebClient) {
        this.webClient = aiWebClient;
    }

    public Mono<ChatResponse> processChat(String query) {
        return webClient.post()
                .uri("/api/chat")
                .bodyValue(new ChatRequest(query))
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> Mono.error(new RuntimeException("AI Service Error"))
                )
                .bodyToMono(ChatResponse.class);
    }
}
