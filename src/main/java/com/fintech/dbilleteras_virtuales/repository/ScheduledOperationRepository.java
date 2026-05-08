package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import java.time.LocalDateTime;


public interface ScheduledOperationRepository extends MongoRepository<ScheduledOperation, String>{
    List<ScheduledOperation> findByUserId(String userId);
    List<ScheduledOperation> findByExecuted(boolean executed);
    List<ScheduledOperation> findByScheduledDate(LocalDateTime scheduledDate);
    List<ScheduledOperation> findByExecutedFalseAndScheduledDateBefore(LocalDateTime date);
}