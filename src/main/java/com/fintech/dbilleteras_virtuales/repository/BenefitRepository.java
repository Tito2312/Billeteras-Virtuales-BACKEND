package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.Benefit;

public interface BenefitRepository extends MongoRepository<Benefit, String> {
    Optional<Benefit> findByCode(String code);
    List<Benefit> findByActiveTrue();
}
