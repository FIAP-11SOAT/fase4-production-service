package com.example.production.repository;

import com.example.production.enums.ProductionStatus;
import com.example.production.model.Production;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductionRepository {
    
    private final DynamoDbEnhancedClient enhancedClient;
    
    @Value("${AWS_DYNAMODB_TABLE_NAME}")
    private String tableName;
    
    private DynamoDbTable<Production> getTable() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Production.class));
    }
    
    public Production save(Production production) {
        if (production.getId() == null) {
            production.setId(UUID.randomUUID().toString());
        }
        production.setCreatedAtIfNull();
        
        log.debug("Saving production: {}", production.getId());
        getTable().putItem(production);
        return production;
    }
    
    public Optional<Production> findById(String id) {
        log.debug("Finding production by id: {}", id);
        Production item = getTable().getItem(Key.builder().partitionValue(id).build());
        return Optional.ofNullable(item);
    }
    
    public Optional<Production> findByOrderId(Long orderId) {
        log.debug("Finding production by orderId: {}", orderId);
        
        DynamoDbIndex<Production> orderIdIndex = getTable().index("OrderIdIndex");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(orderId.toString()).build()
        );
        
        return orderIdIndex.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
    
    public List<Production> findByStatus(ProductionStatus status) {
        log.debug("Finding productions by status: {}", status);
        
        DynamoDbIndex<Production> statusIndex = getTable().index("StatusIndex");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
            Key.builder().partitionValue(status.name()).build()
        );
        
        return statusIndex.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }
    
    public List<Production> findByStatusIn(List<ProductionStatus> statuses) {
        log.debug("Finding productions by statuses: {}", statuses);
        
        return statuses.stream()
                .flatMap(status -> findByStatus(status).stream())
                .collect(Collectors.toList());
    }
    
    public List<Production> findByStartedAtBetween(Instant start, Instant end) {
        log.debug("Finding productions by startedAt between: {} and {}", start, end);
        
        // Para este tipo de consulta complexa, seria necessário usar Scan
        // ou criar um GSI adicional com startedAt como sort key
        return getTable().scan()
                .items()
                .stream()
                .filter(production -> production.getStartedAt() != null)
                .filter(production -> 
                    production.getStartedAt().isAfter(start) || production.getStartedAt().equals(start))
                .filter(production -> 
                    production.getStartedAt().isBefore(end) || production.getStartedAt().equals(end))
                .collect(Collectors.toList());
    }
    
    public List<Production> findByStatusAndStartedAtAfter(ProductionStatus status, Instant after) {
        log.debug("Finding productions by status: {} and startedAt after: {}", status, after);
        
        return findByStatus(status)
                .stream()
                .filter(production -> production.getStartedAt() != null)
                .filter(production -> production.getStartedAt().isAfter(after))
                .collect(Collectors.toList());
    }
    
    public long countByStatus(ProductionStatus status) {
        log.debug("Counting productions by status: {}", status);
        
        return findByStatus(status).size();
    }
    
    public boolean existsByOrderId(Long orderId) {
        log.debug("Checking if production exists by orderId: {}", orderId);
        
        return findByOrderId(orderId).isPresent();
    }
    
    public void delete(Production production) {
        log.debug("Deleting production: {}", production.getId());
        getTable().deleteItem(Key.builder().partitionValue(production.getId()).build());
    }
    
    public void deleteById(String id) {
        log.debug("Deleting production by id: {}", id);
        getTable().deleteItem(Key.builder().partitionValue(id).build());
    }
    
    public List<Production> findAll() {
        log.debug("Finding all productions");
        return getTable().scan().items().stream().collect(Collectors.toList());
    }
}
