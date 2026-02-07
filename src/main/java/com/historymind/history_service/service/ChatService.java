package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatService {
    private final WebClient webClient;

    public ChatService(WebClient.Builder webClientBuilder) {
        // Build WebClient từ Builder được tiêm vào
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build();
    }

    public Mono<ChatResponse> processChat(String query) {
        return webClient.post()
                .uri("/chat")
                .bodyValue(new ChatRequest(query))
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        Mono.error(new RuntimeException("FastAPI Service Error")))
                .bodyToMono(ChatResponse.class);
    }
}
