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
public class OutgoingEvent {
    
    @JsonProperty("accepted_events")
    private List<String> acceptedEvents;
    
    private EventMeta meta;
    
    private Map<String, Object> payload;
    
    public static OutgoingEvent createProductionEvent(String eventName, Map<String, Object> payload) {
        return OutgoingEvent.builder()
                .acceptedEvents(List.of(
                    "payment-created-event",
                    "payment-completed-event", 
                    "payment-failed-event",
                    "production-started-event",
                    "production-completed-event"
                ))
                .meta(EventMeta.createForProduction(eventName, "order-service"))
                .payload(payload)
                .build();
    }
}