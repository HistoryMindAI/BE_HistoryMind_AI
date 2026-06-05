package com.historymind.history_service.controller;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PingControllerTest {
    @Test
    void testPing() {
        PingController controller = new PingController();
        assertNotNull(controller); // tests constructor
        StepVerifier.create(controller.ping())
                .expectNext("pong")
                .verifyComplete();
    }
}
