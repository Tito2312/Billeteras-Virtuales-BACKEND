package com.fintech.dbilleteras_virtuales.repository;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByReceiverUserId(String receiverUserId);

    List<Transaction> findBySourceWalletOrTargetWallet(String sourceWallet, String targetWallet);

    List<Transaction> findByUserIdOrderByCreatedAtAsc(String userId);

    List<Transaction> findBySourceWalletOrderByCreatedAtAsc(String sourceWallet);

    List<Transaction> findBySourceWalletAndCreatedAtBetweenOrderByCreatedAtAsc(
            String sourceWallet,
            LocalDateTime inicio,
            LocalDateTime fin);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByType(com.fintech.dbilleteras_virtuales.model.TransactionType type);

    List<Transaction> findByUserIdAndType(String userId, com.fintech.dbilleteras_virtuales.model.TransactionType type);

    List<Transaction> findByUserIdAndCreatedAtBetween(String userId,
            LocalDateTime inicio,
            LocalDateTime fin);

    List<Transaction> findByTypeAndReceiverUserIdNotNull(TransactionType type);

    List<Transaction> findByTypeAndCreatedAtBetween(TransactionType type, LocalDateTime start, LocalDateTime end);
}
