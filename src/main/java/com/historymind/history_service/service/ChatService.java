package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatRequest;
import com.historymind.history_service.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ChatService {

    private final WebClient webClient;

    public ChatService(WebClient aiWebClient) {
        this.webClient = aiWebClient;
    }

    public Mono<ChatResponse> processChat(String query) {
        log.info("Sending query to AI service: {}", query);
        return webClient.post()
                .uri("/api/chat")
                .bodyValue(new ChatRequest(query))
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> {
                            log.error("AI Service returned error status: {}", response.statusCode());
                            return response.createException()
                                           .flatMap(e -> Mono.error(new RuntimeException("AI Service Error: " + response.statusCode(), e)));
                        }
                )
                .bodyToMono(ChatResponse.class)
                .doOnError(e -> log.error("Error during AI service call", e));
    }
}
