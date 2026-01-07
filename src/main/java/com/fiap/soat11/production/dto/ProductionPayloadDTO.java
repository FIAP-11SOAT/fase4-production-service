package com.fiap.soat11.production.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionPayloadDTO {
    
    @JsonProperty("order_id")
    private String orderId;
}
