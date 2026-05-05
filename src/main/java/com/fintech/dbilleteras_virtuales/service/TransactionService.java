package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;


    public Transaction findById(String id){
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction recharge(String userId, String targetWallet, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTargetWallet(targetWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.RECHARGE);
        return transactionRepository.save(transaction);
    }

    public Transaction withdrawal(String userId, String sourceWallet, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setSourceWallet(sourceWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        return transactionRepository.save(transaction);
    }

    public Transaction transfer(String userId, String sourceWallet, String targetWallet, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setSourceWallet(sourceWallet);
        transaction.setTargetWallet(targetWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        return transactionRepository.save(transaction);
    }

    public Transaction reverseTransaction(String transactionId) {
        Transaction transaction = findById(transactionId);
        transaction.setReversed(true);
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getHistoryByUserId(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getHistoryByWalletId(String walletId) {
        return transactionRepository.findBySourceWalletOrTargetWallet(walletId, walletId);
    }

}
