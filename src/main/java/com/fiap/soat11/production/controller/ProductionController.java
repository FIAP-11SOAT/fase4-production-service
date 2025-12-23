package com.fiap.soat11.production.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.soat11.production.dto.ErrorResponse;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fiap.soat11.production.service.ProductionProducerService;

@RestController
@RequestMapping("/api/v1/productions")
public class ProductionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionController.class);

    private final ProductionProducerService productionProducerService;

    public ProductionController(ProductionProducerService productionProducerService) {
        this.productionProducerService = productionProducerService;
    }

    @PutMapping("/{productionId}/status")
    public ResponseEntity<?> updateProductionStatus(
            @PathVariable String productionId,
            @RequestBody StatusUpdateRequest statusRequest) {

        try {
            logger.info("Recebida requisição para atualizar status da Production: {} para: {}",
                    productionId, statusRequest.getStatus());

            Production updatedProduction = productionProducerService
                    .updateStatusAndPublish(productionId, statusRequest.getStatus());

            logger.info("Production {} atualizada com sucesso para status: {}",
                    productionId, statusRequest.getStatus());

            return ResponseEntity.ok(updatedProduction);

        } catch (ProductionException ex) {
            logger.error("Erro ao atualizar Production: {}", ex.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception ex) {
            logger.error("Erro inesperado ao atualizar Production: {}", ex.getMessage(), ex);
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public static class StatusUpdateRequest {
        private String status;

        public StatusUpdateRequest() {
        }

        public StatusUpdateRequest(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
