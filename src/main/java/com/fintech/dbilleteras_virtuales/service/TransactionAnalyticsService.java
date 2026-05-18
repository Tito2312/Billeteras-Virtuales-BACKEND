package com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.LinkedList;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.ListNode;
import com.fintech.dbilleteras_virtuales.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAnalyticsService {

    public final TransactionRepository transactionRepository;
    public final UserRepository userRepository;
    public final NotificationService notificationService;

    public List<Transaction> historytransactions(String userId) {

        List<Transaction> listTransactions = transactionRepository.findByUserId(userId);
        return listTransactions;

    }

    public double averageTransactions(String userId) {

        List<Transaction> list = historytransactions(userId);

        LinkedList<Transaction> transactionsListaSimple = new LinkedList<>();

        for (Transaction t : list) {
            transactionsListaSimple.agregar(t);

        }

        double suma = 0;
        int count = transactionsListaSimple.getTamaño();

        ListNode<Transaction> firstNode = transactionsListaSimple.firtNodo();

        while (firstNode != null) {

            suma += firstNode.getValorNodo().getAmount();
            firstNode = firstNode.getSiguienteNodo();

        }

        double promedio = suma / count;

        return promedio;

    }

    public void anomalyDetection(String userId, double amount) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (amount > averageTransactions(userId)) {

            notificationService.anomalyDetection(user.getEmail());

        }

    }

}
