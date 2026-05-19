package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.Audit;

public interface AuditRepository extends MongoRepository<Audit, String> {

    List<Audit> findByUserId(String userId);

}