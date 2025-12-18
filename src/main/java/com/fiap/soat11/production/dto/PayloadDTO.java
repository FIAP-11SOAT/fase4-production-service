package com.fiap.soat11.production.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class PayloadDTO {
    
    private UUID id;

    private List<ItensDTO> itens;

    private CustomerDTO customer;
}
