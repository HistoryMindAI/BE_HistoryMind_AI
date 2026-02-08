package com.historymind.history_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ChatResponse {

    private String query;
    private String intent;
    private String answer;

    private List<EventDTO> events;

    @JsonProperty("noData")
    @JsonAlias("no_data")
    private boolean noData;
}
