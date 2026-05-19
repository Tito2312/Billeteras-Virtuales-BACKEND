package com.fintech.dbilleteras_virtuales.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.ListaSimple;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.NodoLista;
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

        ListaSimple<Transaction> transactionsListaSimple = new ListaSimple<>();

        for (Transaction t : list) {
            transactionsListaSimple.agregar(t);

        }

        double suma = 0;
        int count = transactionsListaSimple.getTamaño();

        NodoLista<Transaction> firstNode = transactionsListaSimple.firtNodo();

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

        if (amount > averageTransactions(userId) * 2) {

            notificationService.anomalyDetection(user.getEmail());

        }

    }

    public void detectFastTransfers(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime starDay = LocalDate.now().atStartOfDay();
        LocalDateTime endDay = LocalDate.now().atTime(23, 59, 59);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(userId, starDay, endDay);

        ListaSimple<Transaction> listTransaction = new ListaSimple<>();

        for (Transaction t : transactions) {
            listTransaction.agregar(t);

        }

        NodoLista<Transaction> firsNodo = listTransaction.firtNodo();
        int count = 0;

        while (firsNodo != null && firsNodo.getSiguienteNodo() != null) {

            Transaction actual = firsNodo.getValorNodo();
            Transaction siguiente = firsNodo.getSiguienteNodo().getValorNodo();

            long minutes = Duration.between(actual.getCreatedAt(), siguiente.getCreatedAt()).toMinutes();

            if (minutes < 5) {
                count++;

                if (count > 5) {
                    notificationService.anomalyDetection(user.getEmail());

                }

            } else {
                count = 0;
            }
            firsNodo = firsNodo.getSiguienteNodo();

        }

    }

    public void detectRepetitiveTransfers(String userId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDateTime starDay = LocalDate.now().atStartOfDay();
        LocalDateTime endDay = LocalDate.now().atTime(23, 59, 59);
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(userId, starDay, endDay);

        ListaSimple<Transaction> listTransaction = new ListaSimple<>();

        for (Transaction t : transactions) {
            listTransaction.agregar(t);

        }

        NodoLista<Transaction> firsNodo = listTransaction.firtNodo();
        int count = 0;

        while (firsNodo != null && firsNodo.getSiguienteNodo() != null) {

            Transaction actual = firsNodo.getValorNodo();
            Transaction siguiente = firsNodo.getSiguienteNodo().getValorNodo();

            String userReceiverId = firsNodo.getValorNodo().getReceiverUserId();
            String userReceiverId2 = firsNodo.getSiguienteNodo().getValorNodo().getReceiverUserId();

            long minutes = Duration.between(actual.getCreatedAt(), siguiente.getCreatedAt()).toMinutes();

            if (minutes < 5 && userReceiverId != null && userReceiverId.equals(userReceiverId2)) {
                count++;

                if (count > 5) {
                    notificationService.anomalyDetection(user.getEmail());

                }

            } else {
                count = 0;
            }
            firsNodo = firsNodo.getSiguienteNodo();

        }

    }

}
