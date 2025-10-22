package com.example.production.repository;

import com.example.production.enums.ProductionStatus;
import com.example.production.model.Production;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductionRepository extends MongoRepository<Production, String> {
    
    Optional<Production> findByOrderId(Long orderId);
    
    List<Production> findByStatus(ProductionStatus status);
    
    List<Production> findByStatusIn(List<ProductionStatus> statuses);
    
    @Query("{'startedAt': {$gte: ?0, $lte: ?1}}")
    List<Production> findByStartedAtBetween(Instant start, Instant end);
    
    @Query("{'status': ?0, 'startedAt': {$gte: ?1}}")
    List<Production> findByStatusAndStartedAtAfter(ProductionStatus status, Instant after);
    
    long countByStatus(ProductionStatus status);
    
    boolean existsByOrderId(Long orderId);
}
