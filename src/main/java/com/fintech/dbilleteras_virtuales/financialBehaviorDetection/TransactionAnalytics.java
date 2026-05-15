package com.fintech.dbilleteras_virtuales.financialBehaviorDetection;

import java.util.List;

import com.fintech.dbilleteras_virtuales.dataStructure.ListaSimple;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.NodoLista;
import com.fintech.dbilleteras_virtuales.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionAnalytics {

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
            firstNode.getSiguienteNodo();

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
