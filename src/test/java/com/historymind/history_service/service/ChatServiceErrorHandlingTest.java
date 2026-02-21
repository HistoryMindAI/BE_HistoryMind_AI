package com.historymind.history_service.service;

import com.historymind.history_service.dto.ChatResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatService Error Handling Tests
 *
 * Tests for error scenarios when communicating with AI backend:
 * - AI Service timeout
 * - AI Service returns 500 error
 * - Malformed AI response
 * - Empty response body
 * - WebClient connection failure
 * - Network interruptions
 *
 * These tests ensure the backend gracefully handles AI service failures
 * and provides meaningful error messages to the frontend.
 */
public class ChatServiceErrorHandlingTest {

        private MockWebServer mockWebServer;
        private ChatService chatService;

        @BeforeEach
        void setUp() throws IOException {
                mockWebServer = new MockWebServer();
                mockWebServer.start();
                String baseUrl = mockWebServer.url("/").toString();

                // Configure WebClient with short timeout for testing
                WebClient webClient = WebClient.builder()
                                .baseUrl(baseUrl)
                                .build();

                chatService = new ChatService(webClient);
        }

        @AfterEach
        void tearDown() throws IOException {
                try {
                        mockWebServer.shutdown();
                } catch (IOException e) {
                        // Ignore shutdown errors — some tests disconnect early
                }
        }

        // ========================================================================
        // AI Service Timeout Tests
        // ========================================================================

        @Test
        void processChat_AIServiceTimeout_ShouldHandleGracefully() {
                // Simulate slow AI service response
                mockWebServer.enqueue(new MockResponse()
                                .setBodyDelay(10, TimeUnit.SECONDS)
                                .setBody("{}"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                // Should timeout and propagate error
                StepVerifier.create(result.timeout(Duration.ofMillis(500)))
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_AIServiceTimeoutWithPartialResponse_ShouldHandleGracefully() {
                // Simulate AI service that starts responding but times out mid-stream
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\",") // Incomplete JSON
                                .setBodyDelay(10, TimeUnit.SECONDS)
                                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result.timeout(Duration.ofMillis(500)))
                                .expectError()
                                .verify();
        }

        // ========================================================================
        // AI Service Error Response Tests (500, 502, 503, 504)
        // ========================================================================

        @Test
        void processChat_AIService500Error_ShouldPropagateError() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(500)
                                .setBody("{\"error\": \"Internal server error\"}"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        @Test
        void processChat_AIService502BadGateway_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(502)
                                .setBody("Bad Gateway"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        @Test
        void processChat_AIService503ServiceUnavailable_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(503)
                                .setBody("Service Unavailable"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        @Test
        void processChat_AIService504GatewayTimeout_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(504)
                                .setBody("Gateway Timeout"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        @Test
        void processChat_AIService429TooManyRequests_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(429)
                                .setBody("{\"error\": \"Rate limit exceeded\"}"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        // ========================================================================
        // Malformed AI Response Tests
        // ========================================================================

        @Test
        void processChat_MalformedJSON_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{invalid json}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_IncompleteJSON_ShouldHandleGracefully() {
                // JSON that's cut off mid-object
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\", \"answer\":")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_JSONWithUnexpectedStructure_ShouldHandleGracefully() {
                // Valid JSON but unexpected structure
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"unexpected\": \"structure\", \"missing\": \"fields\"}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        // Should parse successfully but have null/empty fields
                                        assertNull(response.getQuery());
                                        assertNull(response.getAnswer());
                                })
                                .verifyComplete();
        }

        @Test
        void processChat_JSONArrayInsteadOfObject_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("[]")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_JSONNullValue_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("null")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                // "null" JSON body deserializes to null — bodyToMono completes with no emission
                StepVerifier.create(result)
                                .verifyComplete();
        }

        // ========================================================================
        // Empty Response Body Tests
        // ========================================================================

        @Test
        void processChat_EmptyResponseBody_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                // Empty body → bodyToMono completes with no emission (empty Mono)
                StepVerifier.create(result)
                                .verifyComplete();
        }

        @Test
        void processChat_WhitespaceOnlyResponseBody_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("   \n\t   ")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_EmptyJSONObject_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        // Should parse but have all null fields
                                        assertNull(response.getQuery());
                                        assertNull(response.getAnswer());
                                        assertNull(response.getIntent());
                                })
                                .verifyComplete();
        }

        // ========================================================================
        // WebClient Connection Failure Tests
        // ========================================================================

        @Test
        void processChat_ConnectionRefused_ShouldPropagateError() throws IOException {
                // Stop the mock server to simulate connection refused
                mockWebServer.shutdown();

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError(WebClientRequestException.class)
                                .verify();
        }

        @Test
        void processChat_UnknownHost_ShouldHandleGracefully() {
                // Create ChatService with invalid host
                WebClient invalidWebClient = WebClient.builder()
                                .baseUrl("http://invalid-host-that-does-not-exist-12345.com")
                                .build();
                ChatService invalidChatService = new ChatService(invalidWebClient);

                Mono<ChatResponse> result = invalidChatService.processChat("test query");

                StepVerifier.create(result.timeout(Duration.ofSeconds(5)))
                                .expectError()
                                .verify();
        }

        @Test
        void processChat_NetworkInterruption_ShouldHandleGracefully() {
                // Simulate network interruption mid-response
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\",")
                                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .expectError()
                                .verify();
        }

        // ========================================================================
        // Content-Type Mismatch Tests
        // ========================================================================

        @Test
        void processChat_WrongContentType_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\", \"answer\": \"test\"}")
                                .addHeader("Content-Type", "text/html")); // Wrong content type

                Mono<ChatResponse> result = chatService.processChat("test query");

                // WebClient throws WebClientResponseException when Content-Type
                // doesn't match expected (cannot deserialize text/html to ChatResponse)
                StepVerifier.create(result)
                                .expectError(WebClientResponseException.class)
                                .verify();
        }

        @Test
        void processChat_MissingContentType_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\", \"answer\": \"test\"}"));
                // No Content-Type header → WebClient cannot determine decoder

                Mono<ChatResponse> result = chatService.processChat("test query");

                // Missing Content-Type → WebClient throws WebClientResponseException
                StepVerifier.create(result)
                                .expectError(WebClientResponseException.class)
                                .verify();
        }

        // ========================================================================
        // Large Response Handling Tests
        // ========================================================================

        @Test
        void processChat_VeryLargeResponse_ShouldHandleGracefully() {
                // Generate moderately large response (~200KB, within default buffer limits)
                StringBuilder largeAnswer = new StringBuilder();
                for (int i = 0; i < 10000; i++) {
                        largeAnswer.append("Long history text. ");
                }

                String jsonResponse = String.format(
                                "{\"query\": \"test\", \"answer\": \"%s\", \"events\": [], \"no_data\": false}",
                                largeAnswer.toString().replace("\"", "\\\""));

                mockWebServer.enqueue(new MockResponse()
                                .setBody(jsonResponse)
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        assertNotNull(response.getAnswer());
                                        assertTrue(response.getAnswer().length() > 10000);
                                })
                                .verifyComplete();
        }

        // ========================================================================
        // Special Characters and Encoding Tests
        // ========================================================================

        @Test
        void processChat_VietnameseCharactersInError_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(500)
                                .setBody("{\"error\": \"Lỗi hệ thống: Không thể xử lý yêu cầu\"}")
                                .addHeader("Content-Type", "application/json; charset=utf-8"));

                Mono<ChatResponse> result = chatService.processChat("Năm 1945");

                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();
        }

        @Test
        void processChat_EmojiInResponse_ShouldHandleGracefully() {
                String jsonResponse = """
                                {
                                    "query": "test",
                                    "answer": "Xin chào! 👋 Tôi là History Mind AI 🇻🇳",
                                    "events": [],
                                    "no_data": false
                                }
                                """;

                mockWebServer.enqueue(new MockResponse()
                                .setBody(jsonResponse)
                                .addHeader("Content-Type", "application/json; charset=utf-8"));

                Mono<ChatResponse> result = chatService.processChat("test");

                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        assertTrue(response.getAnswer().contains("👋"));
                                        assertTrue(response.getAnswer().contains("🇻🇳"));
                                })
                                .verifyComplete();
        }

        // ========================================================================
        // Retry Logic Tests (if implemented)
        // ========================================================================

        @Test
        void processChat_TransientError_ShouldNotRetryByDefault() {
                // First request fails, second succeeds
                mockWebServer.enqueue(new MockResponse().setResponseCode(503));
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"test\", \"answer\": \"success\"}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("test query");

                // Should fail on first error (no retry by default)
                StepVerifier.create(result)
                                .expectError(RuntimeException.class)
                                .verify();

                // Verify only one request was made
                assertEquals(1, mockWebServer.getRequestCount());
        }

        // ========================================================================
        // Null/Empty Input Tests
        // ========================================================================

        @Test
        void processChat_NullQuery_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": null, \"answer\": \"answer\", \"events\": []}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat(null);

                // Should not crash, but may produce error or empty result
                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        // Just verify it doesn't crash
                                        assertNotNull(response);
                                })
                                .verifyComplete();
        }

        @Test
        void processChat_EmptyQuery_ShouldHandleGracefully() {
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{\"query\": \"\", \"answer\": \"answer\", \"events\": []}")
                                .addHeader("Content-Type", "application/json"));

                Mono<ChatResponse> result = chatService.processChat("");

                StepVerifier.create(result)
                                .consumeNextWith(response -> {
                                        assertNotNull(response);
                                })
                                .verifyComplete();
        }
}
