package com.historymind.history_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatResponse {
    private String query;
    private String intent;
    private String answer;
    private List<EventDTO> events;
    private boolean no_data;
}