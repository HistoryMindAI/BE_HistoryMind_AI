package com.historymind.history_service.dto;

import lombok.Data;

@Data
public class EventDTO {
    private Integer id;
    private Integer year;
    private String event;
    private String tone;
    private String story;
}