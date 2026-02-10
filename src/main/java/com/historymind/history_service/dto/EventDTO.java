package com.historymind.history_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO {
    private String id;
    private Integer year;
    private String event;
    private String story;
    private String tone;
    private String title;
    private List<String> persons;
    private List<String> places;
    private List<String> keywords;
}