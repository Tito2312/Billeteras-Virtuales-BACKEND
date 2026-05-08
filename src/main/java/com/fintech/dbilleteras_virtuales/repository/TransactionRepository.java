package com.fintech.dbilleteras_virtuales.repository;

import com.fintech.dbilleteras_virtuales.model.Transaction;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByReceiverUserId(String receiverUserId);
    List<Transaction> findBySourceWalletOrTargetWallet(String sourceWallet, String targetWallet);
}
