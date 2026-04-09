package com.historymind.history_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiWebClient(
            @Value("${ai.service.url}") String aiServiceUrl,
            @Value("${ai.service.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${ai.service.response-timeout-seconds:20}") long responseTimeoutSeconds,
            @Value("${ai.service.read-timeout-seconds:20}") long readTimeoutSeconds,
            @Value("${ai.service.write-timeout-seconds:20}") long writeTimeoutSeconds,
            @Value("${ai.service.pending-acquire-timeout-seconds:5}") long pendingAcquireTimeoutSeconds,
            @Value("${ai.service.max-idle-time-seconds:30}") long maxIdleTimeSeconds,
            @Value("${ai.service.max-life-time-seconds:300}") long maxLifeTimeSeconds,
            @Value("${ai.service.max-connections:200}") int maxConnections,
            @Value("${ai.service.max-in-memory-size-mb:4}") int maxInMemorySizeMb
    ) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ai-webclient-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofSeconds(pendingAcquireTimeoutSeconds))
                .maxIdleTime(Duration.ofSeconds(maxIdleTimeSeconds))
                .maxLifeTime(Duration.ofSeconds(maxLifeTimeSeconds))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(Math.toIntExact(readTimeoutSeconds)))
                        .addHandlerLast(new WriteTimeoutHandler(Math.toIntExact(writeTimeoutSeconds))));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(maxInMemorySizeMb * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(aiServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
