package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.Wallet;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    List<Wallet> findAllByUserId(String userId);
    List<Wallet> findByid(String id);
}
