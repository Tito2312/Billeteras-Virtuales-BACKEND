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
                .findByUserIdAndCreatedAtBetween(userId, startDay, finalDay);

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
                transaction.setOriginalAmount(amount);
                transaction.setCommissionAmount(0);
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
                failedTransaction.setOriginalAmount(amount);
                failedTransaction.setCommissionAmount(0);
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
                transaction.setOriginalAmount(amount);
                transaction.setCommissionAmount(0);
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
                failedTransaction.setOriginalAmount(amount);
                failedTransaction.setCommissionAmount(0);
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

    private double getCommissionRateByLevel(String level) {
        System.out.println("🔍 Nivel del usuario: " + level);
        switch (level) {
            case "Bronce":
                return 0.05;
            case "Plata":
                return 0.03;
            case "Silver":
                return 0.03;
            case "Oro":
                return 0.015;
            case "Gold":
                return 0.015;
            case "Platino":
                return 0.0;
            case "Platinum":
                return 0.0;
            default:
                return 0.05;
        }
    }

    public Transaction transfer(String userId, String sourceWallet, String transferKey, double amount) {
        if (!validateTransaction(userId)) {
            throw new RuntimeException("Límite de transacciones diarias alcanzado");
        }

        try {
            if (amount <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            Wallet targetWalletObj = walletService.findByTransferKey(transferKey);
            if (targetWalletObj == null) {
                throw new RuntimeException("Billetera destino no encontrada con esa clave");
            }

            String targetWallet = targetWalletObj.getId();
            String receiverUserId = targetWalletObj.getUserId();

            if (sourceWallet.equals(targetWallet)) {
                throw new RuntimeException("No se puede transferir a la misma billetera");
            }

            if (!walletService.hasSufficientBalance(sourceWallet, userId, amount)) {
                throw new RuntimeException("Saldo insuficiente en la billetera origen");
            }
            walletService.validateWalletExists(sourceWallet, userId);

            if (!targetWalletObj.isActive()) {
                throw new RuntimeException("La billetera destino está inactiva");
            }

            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            double commissionRate = getCommissionRateByLevel(user.getLevel());
            double commissionAmount = amount * commissionRate;
            double receiverAmount = amount - commissionAmount; // Lo que realmente recibe el destino

            // 6. Crear transacción
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setReceiverUserId(receiverUserId);
            transaction.setSourceWallet(sourceWallet);
            transaction.setTargetWallet(targetWallet);
            transaction.setAmount(receiverAmount); // Monto que recibe el destinatario
            transaction.setOriginalAmount(amount); // Monto original enviado
            transaction.setCommissionAmount(commissionAmount); // Comisión cobrada
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setStatus(TransactionStatus.COMPLETED);

            int points = rewardService.calculatePoints(transaction.getType(), amount);
            transaction.setPoints(points);

            Transaction savedTransaction = transactionRepository.save(transaction);

            walletService.updateBalance(sourceWallet, userId, -amount);
            walletService.updateBalance(targetWallet, receiverUserId, receiverAmount);

            rewardService.updateUserPoints(userId, points);

            if (user != null) {
                notificationService.notificationTransaction(user.getEmail(), "TRANSFERENCIA ENVIADA", amount);
            }

            var receiver = userRepository.findById(receiverUserId).orElse(null);
            if (receiver != null) {
                notificationService.notificationTransaction(receiver.getEmail(), "TRANSFERENCIA RECIBIDA",
                        receiverAmount);
            }

            TransactionAnalyticsService.anomalyDetection(userId, amount, sourceWallet);
            TransactionAnalyticsService.detectRepetitiveTransfers(userId);
            TransactionAnalyticsService.detectFastTransfers(userId);
            TransactionAnalyticsService.detectNocturnalActivity(userId);

            return savedTransaction;

        } catch (Exception e) {

            Transaction failedTransaction = new Transaction();
            failedTransaction.setUserId(userId);
            failedTransaction.setSourceWallet(sourceWallet);
            failedTransaction.setAmount(amount);
            failedTransaction.setOriginalAmount(amount);
            failedTransaction.setCommissionAmount(0);
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

    public Transaction reverseTransaction(String userId, String transactionId) {
        Transaction transaction = findById(transactionId);

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new RuntimeException("La transacción ya fue revertida");
        }

        double originalAmount = transaction.getOriginalAmount() > 0 ? transaction.getOriginalAmount()
                : transaction.getAmount();

        double receiverReceived = transaction.getAmount();
        double commissionPaid = transaction.getCommissionAmount() > 0 ? transaction.getCommissionAmount()
                : (originalAmount - receiverReceived);

        walletService.updateBalance(transaction.getSourceWallet(), transaction.getUserId(), originalAmount);

        if (transaction.getTargetWallet() != null && transaction.getReceiverUserId() != null) {
            walletService.updateBalance(transaction.getTargetWallet(), transaction.getReceiverUserId(),
                    -receiverReceived);
        }

        transaction.setReversed(true);
        transaction.setStatus(TransactionStatus.REVERSED);
        transaction.setReversedAt(LocalDateTime.now());

        rewardService.updateUserPoints(transaction.getUserId(), -transaction.getPoints());

        Transaction saveTransaction = transactionRepository.save(transaction);

        var user = userRepository.findById(transaction.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        notificationService.TransactionReverse(user.getEmail(), transaction.getPoints());

        if (transaction.getReceiverUserId() != null) {
            var receiver = userRepository.findById(transaction.getReceiverUserId()).orElse(null);
            if (receiver != null) {
                notificationService.notificationTransaction(receiver.getEmail(), "TRANSFERENCIA REVERTIDA",
                        receiverReceived);
            }
        }

        return saveTransaction;
    }

    public List<Transaction> getHistoryByUserId(String userId) {
        List<Transaction> asSender = transactionRepository.findByUserId(userId);
        List<Transaction> asReceiver = transactionRepository.findByReceiverUserId(userId);
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(asSender);
        allTransactions.addAll(asReceiver);
        allTransactions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return allTransactions;
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

    public Queue<Transaction> historyQueue(String userId) {

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