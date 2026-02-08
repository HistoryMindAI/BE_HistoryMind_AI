package com.historymind.history_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("🚀 HISTORY SERVICE STARTED SUCCESSFULLY");
        System.out.println("✅ Listening on 0.0.0.0, Port configured via env");
        System.out.println("=================================================");
    }
}
