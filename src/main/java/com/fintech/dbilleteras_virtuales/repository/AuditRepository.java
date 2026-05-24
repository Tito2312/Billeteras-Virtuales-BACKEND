package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.Audit;
import java.time.LocalDateTime;

public interface AuditRepository extends MongoRepository<Audit, String> {

    List<Audit> findByUserId(String userId);

    List<Audit> findByDate(LocalDateTime date);

    List<Audit> findByDateBetween(LocalDateTime start, LocalDateTime end);

    List<Audit> findByNivelRiesgo(String nivelRiesgo);

    List<Audit> findByUserIdAndDateBetween(String userId, LocalDateTime start, LocalDateTime end);

    List<Audit> findByNivelRiesgoAndDateBetween(String nivelRiesgo, LocalDateTime start, LocalDateTime end);

}
