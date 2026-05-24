package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.RedeemedBenefit;

public interface RedeemedBenefitRepository extends MongoRepository<RedeemedBenefit, String> {
    List<RedeemedBenefit> findByUserId(String userId);
    List<RedeemedBenefit> findByUserIdAndStatus(String userId, String status);
}
