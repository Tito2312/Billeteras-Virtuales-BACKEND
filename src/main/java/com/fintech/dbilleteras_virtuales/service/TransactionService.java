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
    private final WalletService walletService;

    public Transaction findById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction recharge(String userId, String targetWallet, double amount) {
        try {

            if (amount <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            walletService.validateWalletExists(targetWallet, userId);

            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setTargetWallet(targetWallet);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.RECHARGE);
            transaction.setStatus(TransactionStatus.COMPLETED);

            int points = rewardService.calculatePoints(transaction.getType(), amount);
            transaction.setPoints(points);

            Transaction savedTransaction = transactionRepository.save(transaction);

            walletService.updateBalance(targetWallet, userId, amount);

            rewardService.updateUserPoints(userId, points);
            notificationService.notificationTransaction(user.getEmail(), "RECARGA", amount);

            return savedTransaction;

        } catch (Exception e) {
            // Crear transacción fallida
            Transaction failedTransaction = new Transaction();
            failedTransaction.setUserId(userId);
            failedTransaction.setTargetWallet(targetWallet);
            failedTransaction.setAmount(amount);
            failedTransaction.setType(TransactionType.RECHARGE);
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setPoints(0);

            transactionRepository.save(failedTransaction);

            try {
                var user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    notificationService.rejetedTransaction(user.getEmail(), "RECARGA");
                }
            } catch (Exception notificationEx) {
                System.err.println("Error enviando notificación de rechazo: " + notificationEx.getMessage());
            }

            throw new RuntimeException("Recarga fallida: " + e.getMessage());
        }
    }

    public Transaction withdrawal(String userId, String sourceWallet, double amount) {
        try {

            if (amount <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!walletService.hasSufficientBalance(sourceWallet, userId, amount)) {
                throw new RuntimeException("Saldo insuficiente en la billetera");
            }

            walletService.validateWalletExists(sourceWallet, userId);

            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setSourceWallet(sourceWallet);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setStatus(TransactionStatus.COMPLETED);

            int points = rewardService.calculatePoints(transaction.getType(), amount);
            transaction.setPoints(points);

            Transaction savedTransaction = transactionRepository.save(transaction);

            walletService.updateBalance(sourceWallet, userId, -amount);

            rewardService.updateUserPoints(userId, points);
            notificationService.notificationTransaction(user.getEmail(), "RETIRO", amount);

            return savedTransaction;

        } catch (Exception e) {

            Transaction failedTransaction = new Transaction();
            failedTransaction.setUserId(userId);
            failedTransaction.setSourceWallet(sourceWallet);
            failedTransaction.setAmount(amount);
            failedTransaction.setType(TransactionType.WITHDRAWAL);
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setPoints(0);

            transactionRepository.save(failedTransaction);

            try {
                var user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    notificationService.rejetedTransaction(user.getEmail(), "RETIRO");
                }
            } catch (Exception notificationEx) {
                System.err.println("Error enviando notificación de rechazo: " + notificationEx.getMessage());
            }

            throw new RuntimeException("Retiro fallido: " + e.getMessage());
        }
    }

    public Transaction transfer(String userId, String sourceWallet, String targetWallet, double amount) {
        try {

            if (amount <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            if (sourceWallet.equals(targetWallet)) {
                throw new RuntimeException("No se puede transferir a la misma billetera");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!walletService.hasSufficientBalance(sourceWallet, userId, amount)) {
                throw new RuntimeException("Saldo insuficiente en la billetera origen");
            }

            walletService.validateWalletExists(sourceWallet, userId);
            walletService.validateWalletExists(targetWallet, userId);

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

            walletService.updateBalance(sourceWallet, userId, -amount);
            walletService.updateBalance(targetWallet, userId, amount);

            rewardService.updateUserPoints(userId, points);
            notificationService.notificationTransaction(user.getEmail(), "TRANSFERENCIA", amount);
            return savedTransaction;

        } catch (Exception e) {

            Transaction failedTransaction = new Transaction();
            failedTransaction.setUserId(userId);
            failedTransaction.setSourceWallet(sourceWallet);
            failedTransaction.setTargetWallet(targetWallet);
            failedTransaction.setAmount(amount);
            failedTransaction.setType(TransactionType.TRANSFER);
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setPoints(0);

            transactionRepository.save(failedTransaction);

            try {
                var user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    notificationService.rejetedTransaction(user.getEmail(), "TRANSFERENCIA");
                }
            } catch (Exception notificationEx) {
                System.err.println("Error enviando notificación de rechazo: " + notificationEx.getMessage());
            }

            throw new RuntimeException("Transferencia fallida: " + e.getMessage());
        }
    }

    public Transaction transfer(String userId, String receiverUserId, String sourceWallet, String targetWallet,
            double amount) {
        try {

            if (amount <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            if (sourceWallet.equals(targetWallet)) {
                throw new RuntimeException("No se puede transferir a la misma billetera");
            }

            if (userId.equals(receiverUserId)) {
                throw new RuntimeException(
                        "Use el método de transferencia interna para transferencias entre sus propias billeteras");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario remitente no encontrado"));

            var user2 = userRepository.findById(receiverUserId)
                    .orElseThrow(() -> new RuntimeException("Usuario destinatario no encontrado"));

            if (!walletService.hasSufficientBalance(sourceWallet, userId, amount)) {
                throw new RuntimeException("Saldo insuficiente en la billetera origen");
            }

            walletService.validateWalletExists(sourceWallet, userId);
            walletService.validateWalletExistsForOwner(targetWallet, receiverUserId);

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

            walletService.updateBalance(sourceWallet, userId, -amount);
            walletService.updateBalance(targetWallet, receiverUserId, amount);

            rewardService.updateUserPoints(userId, points);
            notificationService.TransferNotification(user.getEmail(), user.getName(), user2.getEmail(), user2.getName(),
                    amount);
            return savedTransaction;

        } catch (Exception e) {

            Transaction failedTransaction = new Transaction();
            failedTransaction.setUserId(userId);
            failedTransaction.setReceiverUserId(receiverUserId);
            failedTransaction.setSourceWallet(sourceWallet);
            failedTransaction.setTargetWallet(targetWallet);
            failedTransaction.setAmount(amount);
            failedTransaction.setType(TransactionType.TRANSFER);
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setPoints(0);

            transactionRepository.save(failedTransaction);

            try {
                var user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    notificationService.rejetedTransaction(user.getEmail(), "TRANSFERENCIA");
                }
            } catch (Exception notificationEx) {
                System.err.println("Error enviando notificación de rechazo: " + notificationEx.getMessage());
            }

            throw new RuntimeException("Transferencia fallida: " + e.getMessage());
        }
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