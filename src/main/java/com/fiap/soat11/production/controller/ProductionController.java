package com.fiap.soat11.production.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.fiap.soat11.production.dto.ErrorResponse;
import com.fiap.soat11.production.entity.Production;
import com.fiap.soat11.production.exception.ProductionException;
import com.fiap.soat11.production.service.ProductionProducerService;

@RestController
@RequestMapping("/production")
public class ProductionController {

    private static final Logger logger = LoggerFactory.getLogger(ProductionController.class);

    private final ProductionProducerService productionProducerService;

    public ProductionController(ProductionProducerService productionProducerService) {
        this.productionProducerService = productionProducerService;
    }

    @PutMapping("/{productionId}/started")
    public ResponseEntity<?> updateProductionStatusToStarted(@PathVariable String productionId) {

        try {
            logger.debug("Recebida requisição para atualizar status da Production para STARTED");

            Production updatedProduction = productionProducerService
                    .updateStatusAndPublish(productionId, "STARTED");

            logger.debug("Production atualizada com sucesso para STARTED");

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

    @PutMapping("/{productionId}/completed")
    public ResponseEntity<?> updateProductionStatusToCompleted(@PathVariable String productionId) {

        try {
            logger.debug("Recebida requisição para atualizar status da Production para COMPLETED");

            Production updatedProduction = productionProducerService
                    .updateStatusAndPublish(productionId, "COMPLETED");

            logger.debug("Production atualizada com sucesso para COMPLETED");

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

    @GetMapping
    public ResponseEntity<?> listPendingProductions() {
        try {
            logger.debug("Recebida requisição para listar produções pendentes");

            List<Production> productions = productionProducerService.listPendingProductions();

            logger.debug("Listagem de produções pendentes concluída com sucesso. Total: {}", productions.size());

            return ResponseEntity.ok(productions);

        } catch (ProductionException ex) {
            logger.error("Erro ao listar produções: {}", ex.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception ex) {
            logger.error("Erro inesperado ao listar produções: {}", ex.getMessage(), ex);
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
