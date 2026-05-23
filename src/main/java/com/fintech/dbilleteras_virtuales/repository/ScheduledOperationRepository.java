package com.fintech.dbilleteras_virtuales.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperationStatus;

public interface ScheduledOperationRepository extends MongoRepository<ScheduledOperation, String> {
    
    List<ScheduledOperation> findByUserId(String userId);
    
    List<ScheduledOperation> findByExecuted(boolean executed);
    
    List<ScheduledOperation> findByScheduledDate(LocalDateTime scheduledDate);
    
    List<ScheduledOperation> findByExecutedFalseAndScheduledDateBefore(LocalDateTime date);
    
    List<ScheduledOperation> findByStatusAndScheduledDateBefore(ScheduledOperationStatus status, LocalDateTime date);
    
    List<ScheduledOperation> findByStatus(ScheduledOperationStatus status);
    
    @Query("{ 'status': 'PENDING', 'scheduledDate': { $lte: ?0 } }")
    List<ScheduledOperation> findPendingOperationsToExecute(LocalDateTime now);

    Optional<ScheduledOperation> findByIdAndUserId(String id, String userId);
}