package com.fiap.soat11.production.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDTO {
    
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_date")
    private String eventDate;

    @JsonProperty("event_source")
    private String eventSource;

    @JsonProperty("event_target")
    private String eventTarget;

    @JsonProperty("event_name")
    private String eventName;
}
