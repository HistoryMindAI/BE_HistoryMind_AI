package com.historymind.history_service.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.CorsRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WebFluxConfigTest {

    @Test
    void testAddCorsMappings() {
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping(anyString())).thenReturn(registration);
        when(registration.allowedOrigins(any(String[].class))).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders(any(String[].class))).thenReturn(registration);
        when(registration.allowCredentials(anyBoolean())).thenReturn(registration);

        WebFluxConfig webFluxConfig = new WebFluxConfig();
        webFluxConfig.addCorsMappings(registry);

        verify(registry, times(1)).addMapping("/**");
        verify(registration, times(1)).allowedOrigins(
                "https://fe-history-mind-ai.vercel.app",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        );
        verify(registration, times(1)).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(registration, times(1)).allowedHeaders("*");
        verify(registration, times(1)).allowCredentials(true);
    }
}
