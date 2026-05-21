package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.dataStructure.Queue;
import com.fintech.dbilleteras_virtuales.model.TransactionStatus;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.Stack;
import com.fintech.dbilleteras_virtuales.dataStructure.LinkedList;
import com.fintech.dbilleteras_virtuales.dataStructure.ListNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LevelBenefitService levelBenefitService;
    private final RewardService rewardService;
    private final WalletService walletService;
    private final TransactionAnalyticsService TransactionAnalyticsService;

    public Transaction findById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public boolean validateTransaction(String userId) {

        LocalDate today = LocalDate.now();
        LocalDateTime startDay = today.atStartOfDay();
        LocalDateTime finalDay = today.atTime(LocalTime.MAX);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Transaction> listTransactionsToday = transactionRepository
                .findBySourceWalletAndCreatedAtBetweenOrderByCreatedAtAsc(userId, startDay, finalDay);

        int size = listTransactionsToday.size();

        int limite = levelBenefitService.getDailyTransactionLimit(user.getLevel());

        if (size < limite) {
            return true;

        }
        return false;

    }

    public Transaction recharge(String userId, String targetWallet, double amount) {
        if (validateTransaction(userId)) {
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
                transaction.setCreatedAt(LocalDateTime.now());
                transaction.setType(TransactionType.RECHARGE);
                transaction.setStatus(TransactionStatus.COMPLETED);

                int points = rewardService.calculatePoints(transaction.getType(), amount);
                transaction.setPoints(points);

                Transaction savedTransaction = transactionRepository.save(transaction);

                walletService.updateBalance(targetWallet, userId, amount);

                rewardService.updateUserPoints(userId, points);
                notificationService.notificationTransaction(user.getEmail(), "RECARGA", amount);
                TransactionAnalyticsService.anomalyDetection(userId, amount, targetWallet);
                TransactionAnalyticsService.detectNocturnalActivity(userId);

                return savedTransaction;

            } catch (Exception e) {

                Transaction failedTransaction = new Transaction();
                failedTransaction.setUserId(userId);
                failedTransaction.setTargetWallet(targetWallet);
                failedTransaction.setAmount(amount);
                failedTransaction.setCreatedAt(LocalDateTime.now());
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
        throw new RuntimeException("Sin transacciones disponibles, intenlo mañana : ");

    }

    public Transaction withdrawal(String userId, String sourceWallet, double amount) {

        if (validateTransaction(userId)) {
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
                transaction.setCreatedAt(LocalDateTime.now());
                transaction.setType(TransactionType.WITHDRAWAL);
                transaction.setStatus(TransactionStatus.COMPLETED);

                int points = rewardService.calculatePoints(transaction.getType(), amount);
                transaction.setPoints(points);

                Transaction savedTransaction = transactionRepository.save(transaction);

                walletService.updateBalance(sourceWallet, userId, -amount);

                rewardService.updateUserPoints(userId, points);
                notificationService.notificationTransaction(user.getEmail(), "RETIRO", amount);
                TransactionAnalyticsService.anomalyDetection(userId, amount, sourceWallet);
                TransactionAnalyticsService.detectNocturnalActivity(userId);

                return savedTransaction;

            } catch (Exception e) {

                Transaction failedTransaction = new Transaction();
                failedTransaction.setUserId(userId);
                failedTransaction.setSourceWallet(sourceWallet);
                failedTransaction.setAmount(amount);
                failedTransaction.setCreatedAt(LocalDateTime.now());
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
        throw new RuntimeException("Sin transacciones disponibles, intenlo mañana : ");

    }

    public Transaction transfer(String userId, String sourceWallet, String transferKey, double amount) {
    String targetWallet = null;
    String receiverUserId = null;

    if (validateTransaction(userId)) {
        try {
            Wallet targetWalletObj = walletService.findByTransferKey(transferKey);
            targetWallet = targetWalletObj.getId();
            receiverUserId = targetWalletObj.getUserId();

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
                transaction.setCreatedAt(LocalDateTime.now());
                transaction.setType(TransactionType.TRANSFER);
                transaction.setStatus(TransactionStatus.COMPLETED);

                int points = rewardService.calculatePoints(transaction.getType(), amount);
                transaction.setPoints(points);

                Transaction savedTransaction = transactionRepository.save(transaction);

                walletService.updateBalance(sourceWallet, userId, -amount);
                walletService.updateBalance(targetWallet, userId, amount);

                rewardService.updateUserPoints(userId, points);
                notificationService.notificationTransaction(user.getEmail(), "TRANSFERENCIA", amount);
                TransactionAnalyticsService.anomalyDetection(userId, amount, sourceWallet);
                TransactionAnalyticsService.detectRepetitiveTransfers(userId);
                TransactionAnalyticsService.detectFastTransfers(userId);
                TransactionAnalyticsService.detectNocturnalActivity(userId);
                return savedTransaction;

            } catch (Exception e) {

                Transaction failedTransaction = new Transaction();
                failedTransaction.setUserId(userId);
                failedTransaction.setSourceWallet(sourceWallet);
                failedTransaction.setTargetWallet(targetWallet);
                failedTransaction.setAmount(amount);
                failedTransaction.setCreatedAt(LocalDateTime.now());
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
        throw new RuntimeException("Transferencia fallida: ");

    }

    public Transaction reverseTransaction(String userId, String transactionId) {
        Transaction transaction = findById(transactionId);

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new RuntimeException("La transacción ya fue revertida");
        }

        walletService.updateBalance(transaction.getSourceWallet(), userId, transaction.getAmount());

        if (transaction.getTargetWallet() != null) {
            walletService.updateBalance(transaction.getTargetWallet(), transaction.getReceiverUserId(),
                    -transaction.getAmount());
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

    public Stack<Transaction> apilarTransaccionRevertir(String userid) {

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtAsc(userid);

        Stack<Transaction> pilaTransactions = new Stack<>();

        for (Transaction t : transactions) {
            pilaTransactions.push(t);

        }

        return pilaTransactions;

    }

    public List ListTransactions(String userId) {

        LinkedList<Transaction> listaTransactions = historyTransactions(userId);

        List<Transaction> resultado = new ArrayList<>();
        ListNode<Transaction> actual = listaTransactions.firstListNode;

        while (actual != null) {
            resultado.add(actual.getNodeValue());
            actual = actual.getNextNode();
        }

        return resultado;

    }

    public LinkedList<Transaction> historyTransactions(String userid) {

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtAsc(userid);

        LinkedList<Transaction> ListaTransactions = new LinkedList<>();

        for (Transaction t : transactions) {
            ListaTransactions.add(t);

        }

        return ListaTransactions;

    }

    public Transaction reverseTransactionPila(String userId) {

        Stack<Transaction> transactions = apilarTransaccionRevertir(userId);

        if (transactions.isEmpty()) {
            return null;
        }

        Transaction Lasttransaction = transactions.pop();

        Transaction revertida = reverseTransaction(userId, Lasttransaction.getId());

        return revertida;

    }

    public Transaction reverseTransactionList(String userId, String transactionId) {

        LinkedList<Transaction> transactions = historyTransactions(userId);

        if (transactions.isEmpty()) {
            return null;

        }

        Transaction transactionReverse = transactions.searchById(transactionId);

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

    public Queue<Transaction> historyCola(String userId) {

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        Queue<Transaction> queueTransactions = new Queue<>();

        for (Transaction t : transactions) {

            if (t.isReversed() == true) {

                queueTransactions.enqueue(t);

            }

        }

        return queueTransactions;

    }

}