package com.fiap.soat11.production.dto;

import lombok.Data;

@Data
public class ConsumeDTO {
    
    private MetaDTO meta;

    private PayloadDTO payload;
}
