package com.example.production.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMeta {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("event_date")
    private Instant eventDate;
    
    @JsonProperty("event_target")
    private String eventTarget;
    
    @JsonProperty("event_source")
    private String eventSource;
    
    @JsonProperty("event_name")
    private String eventName;
    
    public static EventMeta createForProduction(String eventName, String eventTarget) {
        return EventMeta.builder()
                .eventId(UUID.randomUUID().toString())
                .eventDate(Instant.now())
                .eventTarget(eventTarget)
                .eventSource("production-service")
                .eventName(eventName)
                .build();
    }
}