package com.historymind.history_service.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class StartupLoggerTest {

    @Test
    void testStartupLoggerRunsWithoutException() {
        StartupLogger startupLogger = new StartupLogger();
        assertDoesNotThrow(() -> startupLogger.run());
    }
}
