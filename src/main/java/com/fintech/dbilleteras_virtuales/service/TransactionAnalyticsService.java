package com.fintech.dbilleteras_virtuales.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.LinkedList;
import com.fintech.dbilleteras_virtuales.dataStructure.ListNode;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAnalyticsService {

    public final TransactionRepository transactionRepository;
    public final UserRepository userRepository;
    public final NotificationService notificationService;
    public final AuditService auditService;

    public List<Transaction> historyTransactions(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public double averageTransactions(String userId) {
        List<Transaction> list = historyTransactions(userId);

        LinkedList<Transaction> transactionLinkedList = new LinkedList<>();
        for (Transaction t : list) {
            transactionLinkedList.add(t);
        }

        int count = transactionLinkedList.getSize();
        if (count == 0)
            return 0;

        double sum = 0;
        ListNode<Transaction> current = transactionLinkedList.firstNode();
        while (current != null) {
            sum += current.getNodeValue().getAmount();
            current = current.getNextNode();
        }

        return sum / count;
    }

    public void anomalyDetection(String userId, double amount, String walletId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (amount > averageTransactions(userId) * 2) {
            notificationService.anomalyHighAmount(user.getEmail());
            auditService.registrarEvento(
                    userId,
                    user.getId(),
                    "ALTO",
                    "Transferecnia con un valor mas alto al inusual");
        }
    }

    public void detectFastTransfers(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime starDay = LocalDate.now().atStartOfDay();
        LocalDateTime endDay = LocalDate.now().atTime(23, 59, 59);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(userId, starDay, endDay);

        LinkedList<Transaction> listTransaction = new LinkedList<>();
        for (Transaction t : transactions) {
            listTransaction.add(t);
        }

        ListNode<Transaction> firsNodo = listTransaction.firstNode();
        int count = 0;

        while (firsNodo != null && firsNodo.getNextNode() != null) {

            Transaction actual = firsNodo.getNodeValue();
            Transaction siguiente = firsNodo.getNextNode().getNodeValue();

            long minutes = Duration.between(actual.getCreatedAt(), siguiente.getCreatedAt()).toMinutes();

            if (minutes < 5) {
                count++;
                if (count > 3) {
                    notificationService.anomalyFastTransfers(user.getEmail());
                    auditService.registrarEvento(
                            userId,
                            actual.getId(),
                            "ALTO",
                            "Transferencias  menos de 5 minutos");
                }
            } else {
                count = 0;
            }
            firsNodo = firsNodo.getNextNode();
        }
    }

    public void detectRepetitiveTransfers(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime starDay = LocalDate.now().atStartOfDay();
        LocalDateTime endDay = LocalDate.now().atTime(23, 59, 59);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(userId, starDay, endDay);

        LinkedList<Transaction> listTransaction = new LinkedList<>();
        for (Transaction t : transactions) {
            listTransaction.add(t);
        }

        ListNode<Transaction> firsNodo = listTransaction.firstNode();
        int count = 0;

        while (firsNodo != null && firsNodo.getNextNode() != null) {

            Transaction actual = firsNodo.getNodeValue();
            Transaction siguiente = firsNodo.getNextNode().getNodeValue();

            String userReceiverId = actual.getReceiverUserId();
            String userReceiverId2 = siguiente.getReceiverUserId();

            long minutes = Duration.between(actual.getCreatedAt(), siguiente.getCreatedAt()).toMinutes();

            if (minutes < 5 && userReceiverId != null && userReceiverId.equals(userReceiverId2)) {
                count++;
                if (count > 3) {
                    notificationService.anomalyRepetitiveTransfers(user.getEmail());
                    auditService.registrarEvento(
                            userId,
                            actual.getId(),
                            "MEDIO",
                            "Transferencias repetitivas hacia el mismo destino en menos de 5 minutos");
                }
            } else {
                count = 0;
            }
            firsNodo = firsNodo.getNextNode();
        }
    }

    public void detectNocturnalActivity(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime starDay = LocalDate.now().atStartOfDay();
        LocalDateTime endDay = LocalDate.now().atTime(23, 59, 59);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(userId, starDay, endDay);

        LinkedList<Transaction> listTransaction = new LinkedList<>();

        for (Transaction t : transactions) {
            listTransaction.add(t);

        }

        ListNode<Transaction> firsNodo = listTransaction.firstNode();
        int count = 0;

        while (firsNodo != null) {

            int hora = firsNodo.getNodeValue().getCreatedAt().getHour();

            if (hora >= 23 || hora <= 5) {

                count++;

                if (count > 3) {
                    notificationService.anomalyNocturnalActivity(user.getEmail());
                    auditService.registrarEvento(
                            userId,
                            firsNodo.getNodeValue().getId(),
                            "BAJO",
                            "Actividad nocturna inusual detectada");

                }

            } else {
                count = 0;
            }

            firsNodo = firsNodo.getNextNode();

        }

    }
}
