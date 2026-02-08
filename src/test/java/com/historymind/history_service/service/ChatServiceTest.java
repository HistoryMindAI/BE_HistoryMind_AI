package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;

public class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    public void setup() {
        // We will create the service in each test with a specific mock/stub
    }

    @Test
    public void testProcessChatSuccess() {
        ExchangeFunction exchangeFunction = request -> {
            assert request.url().toString().endsWith("/api/chat");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"query\": \"Hello\", \"answer\": \"Mock Answer\", \"events\": [], \"no_data\": false}")
                    .build());
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        chatService = new ChatService(webClient);

        Mono<ChatResponse> result = chatService.processChat("Hello");

        StepVerifier.create(result)
                .expectNextMatches(response -> "Mock Answer".equals(response.getAnswer()))
                .verifyComplete();
    }

    @Test
    public void testProcessChatError() {
        ExchangeFunction exchangeFunction = request -> {
            return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build());
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        chatService = new ChatService(webClient);

        Mono<ChatResponse> result = chatService.processChat("Error");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
