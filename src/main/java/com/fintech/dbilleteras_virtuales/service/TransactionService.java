package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Transaction findById(String id) {
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
        Transaction savedTransaction = transactionRepository.save(transaction);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificationService.notificationTransaction(user.getEmail(), "RECARGA", amount);

        return savedTransaction;
    }

    public Transaction withdrawal(String userId, String sourceWallet, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setSourceWallet(sourceWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        Transaction savedTransaction = transactionRepository.save(transaction);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificationService.notificationTransaction(user.getEmail(), "RETIRO", amount);

        return savedTransaction;
    }

    public Transaction transfer(String userId, String sourceWallet, String targetWallet, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setSourceWallet(sourceWallet);
        transaction.setTargetWallet(targetWallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        Transaction savedTransaction = transactionRepository.save(transaction);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificationService.notificationTransaction(user.getEmail(), "TRANSFERENCIA", amount);
        return savedTransaction;
    }

    public Transaction transfer(String userId, String receiverUserId, String sourceWallet, String targetWallet,
            double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setReceiverUserId(receiverUserId);
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