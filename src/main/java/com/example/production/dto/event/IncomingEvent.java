package com.example.production.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomingEvent {
    
    @JsonProperty("accepted_events")
    private List<String> acceptedEvents;
    
    private EventMeta meta;
    
    private Map<String, Object> payload;
}