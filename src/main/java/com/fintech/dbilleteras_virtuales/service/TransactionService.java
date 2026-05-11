package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionStatus;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RewardService rewardService;

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
        transaction.setStatus(TransactionStatus.COMPLETED);

        int points = rewardService.calculatePoints(transaction.getType(), amount);
        transaction.setPoints(points);

        Transaction savedTransaction = transactionRepository.save(transaction);

        rewardService.updateUserPoints(userId, points);

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
        transaction.setStatus(TransactionStatus.COMPLETED);

        int points = rewardService.calculatePoints(transaction.getType(), amount);
        transaction.setPoints(points);

        Transaction savedTransaction = transactionRepository.save(transaction);

        rewardService.updateUserPoints(userId, points);

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
        transaction.setStatus(TransactionStatus.COMPLETED);

        int points = rewardService.calculatePoints(transaction.getType(), amount);
        transaction.setPoints(points);

        Transaction savedTransaction = transactionRepository.save(transaction);

        rewardService.updateUserPoints(userId, points);

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
        transaction.setStatus(TransactionStatus.COMPLETED);

        int points = rewardService.calculatePoints(transaction.getType(), amount);
        transaction.setPoints(points);

        Transaction savedTransaction = transactionRepository.save(transaction);

        rewardService.updateUserPoints(userId, points);
        var user2 = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        notificationService.TransferNotification(user.getEmail(), user.getName(), user2.getEmail(), user2.getName(),
                amount);
        return savedTransaction;
    }

    public Transaction reverseTransaction(String userId, String transactionId) {
        Transaction transaction = findById(transactionId);

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new RuntimeException("La transacción ya fue revertida");
        }

        transaction.setReversed(true);
        transaction.setStatus(TransactionStatus.REVERSED);

        rewardService.updateUserPoints(transaction.getUserId(), -transaction.getPoints());

        Transaction saveTransaction = transactionRepository.save(transaction);

        var user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        notificationService.TransactionReverse(user.getEmail(), transaction.getPoints());

        return saveTransaction;
    }

    public List<Transaction> getHistoryByUserId(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getHistoryByWalletId(String walletId) {
        return transactionRepository.findBySourceWalletOrTargetWallet(walletId, walletId);
    }

}