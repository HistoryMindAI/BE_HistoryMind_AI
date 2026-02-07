package com.historymind.history_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // Bean 1: Cung cấp Builder (Nếu Service yêu cầu Builder)
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Bean 2: Cung cấp WebClient đã build sẵn (Nếu Service yêu cầu WebClient)
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8000").build();
    }
}