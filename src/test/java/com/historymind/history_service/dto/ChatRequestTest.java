package com.historymind.history_service.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatRequest DTO Tests
 * 
 * Tests for the ChatRequest data transfer object.
 */
public class ChatRequestTest {

    @Test
    public void testNoArgsConstructor() {
        ChatRequest request = new ChatRequest();
        assertNull(request.getQuery());
    }

    @Test
    public void testAllArgsConstructor() {
        ChatRequest request = new ChatRequest("test query");
        assertEquals("test query", request.getQuery());
    }

    @Test
    public void testSetterAndGetter() {
        ChatRequest request = new ChatRequest();
        request.setQuery("new query");
        assertEquals("new query", request.getQuery());
    }

    @Test
    public void testQueryWithVietnamese() {
        String vietnameseQuery = "Năm 1945 có sự kiện gì?";
        ChatRequest request = new ChatRequest(vietnameseQuery);
        assertEquals(vietnameseQuery, request.getQuery());
    }

    @Test
    public void testQueryWithSpecialCharacters() {
        String specialQuery = "Trần Hưng Đạo & \"Bạch Đằng\" - 1288?";
        ChatRequest request = new ChatRequest(specialQuery);
        assertEquals(specialQuery, request.getQuery());
    }

    @Test
    public void testEmptyQuery() {
        ChatRequest request = new ChatRequest("");
        assertEquals("", request.getQuery());
    }
}
