package com.historymind.history_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WebClientConfigTest {

    @Test
    void testAiWebClientBeanCreation() {
        WebClientConfig config = new WebClientConfig();
        WebClient webClient = config.aiWebClient(
                "http://localhost:8080",
                3000,
                20,
                20,
                20,
                5,
                30,
                300,
                200,
                4
        );
        assertNotNull(webClient);
    }
}
