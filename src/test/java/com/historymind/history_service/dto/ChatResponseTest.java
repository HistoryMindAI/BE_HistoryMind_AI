package com.historymind.history_service.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChatResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDeserializationWithNoDataSnakeCase() throws Exception {
        String json = "{\"query\": \"some query\", \"intent\": \"intent\", \"answer\": \"answer\", \"events\": [], \"no_data\": true}";
        ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

        assertTrue(response.isNoData(), "noData should be true when deserializing 'no_data': true");
    }

    @Test
    public void testDeserializationWithNoDataCamelCase() throws Exception {
        String json = "{\"query\": \"some query\", \"intent\": \"intent\", \"answer\": \"answer\", \"events\": [], \"noData\": true}";
        ChatResponse response = objectMapper.readValue(json, ChatResponse.class);

        assertTrue(response.isNoData(), "noData should be true when deserializing 'noData': true");
    }
}
