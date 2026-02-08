package com.historymind.history_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiWebClient(
            @Value("${ai.service.url}") String aiServiceUrl
    ) {
        return WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }
}
