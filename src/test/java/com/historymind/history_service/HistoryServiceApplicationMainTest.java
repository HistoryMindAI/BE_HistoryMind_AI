package com.historymind.history_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HistoryServiceApplicationMainTest {

    @Test
    void testMain() {
        assertDoesNotThrow(() -> {
            HistoryServiceApplication.main(new String[]{"--spring.main.web-application-type=none", "--server.port=0"});
        });
    }
}
