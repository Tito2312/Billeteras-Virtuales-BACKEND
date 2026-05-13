package com.fintech.dbilleteras_virtuales.service;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionStatus;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.Pila;
import com.fintech.dbilleteras_virtuales.dataStructure.ListaSimple;
import com.fintech.dbilleteras_virtuales.dataStructure.NodoLista;

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

    public Pila<Transaction> apilarTransaccionRevertir(String userid) {

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtAsc(userid);

        Pila<Transaction> pilaTransactions = new Pila<>();

        for (Transaction t : transactions) {
            pilaTransactions.apilar(t);

        }

        return pilaTransactions;

    }

    public List ListTransactions(String userId) {

        ListaSimple<Transaction> listaTransactions = historyTransactions(userId);

        List<Transaction> resultado = new ArrayList<>();
        NodoLista<Transaction> actual = listaTransactions.primerNodoLista;

        while (actual != null) {
            resultado.add(actual.getValorNodo());
            actual = actual.getSiguienteNodo();
        }

        return resultado;

    }

    public ListaSimple<Transaction> historyTransactions(String userid) {

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtAsc(userid);

        ListaSimple<Transaction> ListaTransactions = new ListaSimple<>();

        for (Transaction t : transactions) {
            ListaTransactions.agregar(t);

        }

        return ListaTransactions;

    }

    public Transaction reverseTransactionPila(String userId) {

        Pila<Transaction> transactions = apilarTransaccionRevertir(userId);

        if (!transactions.estaVacia()) {
            return null;
        }

        Transaction Lasttransaction = transactions.desapilar();

        Transaction revertida = reverseTransaction(userId, Lasttransaction.getId());

        return revertida;

    }

    public Transaction reverseTransactionList(String userId, String transactionId) {

        ListaSimple<Transaction> transactions = historyTransactions(userId);

        if (transactions.estaVacia()) {
            return null;

        }

        Transaction transactionReverse = transactions.buscarPorId(transactionId);

        Transaction reverse = reverseTransaction(userId, transactionReverse.getId());

        return reverse;

    }

    public List<Transaction> listWalletsTransactions(String walletId) {

        List<Transaction> transactionsWallet = transactionRepository.findBySourceWalletOrderByCreatedAtAsc(walletId);

        return transactionsWallet;

    }

    public List<Transaction> getTransactionsByWalletAndDateRange(String walletId, String inicio, String fin) {

        LocalDateTime dateFist = LocalDateTime.parse(inicio);
        LocalDateTime dateLast = LocalDateTime.parse(fin);

        List<Transaction> transactionsDateRange = transactionRepository
                .findBySourceWalletAndCreatedAtBetweenOrderByCreatedAtAsc(walletId, dateFist, dateLast);

        return transactionsDateRange;
    }

}