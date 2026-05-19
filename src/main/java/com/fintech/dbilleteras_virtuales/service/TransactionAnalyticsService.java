package com.fintech.dbilleteras_virtuales.service;

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
        if (count == 0) return 0;

        double sum = 0;
        ListNode<Transaction> current = transactionLinkedList.firstNode();
        while (current != null) {
            sum += current.getNodeValue().getAmount();
            current = current.getNextNode();
        }

        return sum / count;
    }

    public void anomalyDetection(String userId, double amount) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (amount > averageTransactions(userId)) {
            notificationService.anomalyDetection(user.getEmail());
        }
    }
}