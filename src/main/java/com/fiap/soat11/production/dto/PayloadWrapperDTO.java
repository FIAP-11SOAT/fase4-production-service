package com.fiap.soat11.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayloadWrapperDTO {
    
    private ProductionPayloadDTO production;
}
