package com.fintech.dbilleteras_virtuales.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.util.List;

import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.dataStructure.LinkedList;
import com.fintech.dbilleteras_virtuales.dataStructure.ListNode;
import com.fintech.dbilleteras_virtuales.dataStructure.PriorityQueue;
import com.fintech.dbilleteras_virtuales.dataStructure.HashTable;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.User;

import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void comparePerformance() {

        List<Transaction> transactions = transactionRepository.findAll();
        List<User> users = userRepository.findAll();

        System.out.println("\n===== COMPARACIÓN DE RENDIMIENTO =====");

        // 1. LinkedList vs PriorityQueue — top transacciones por monto
        long start1 = System.nanoTime();
        LinkedList<Transaction> lista = new LinkedList<>();
        for (Transaction t : transactions) lista.add(t);
        Transaction maxLinked = null;
        ListNode<Transaction> node = lista.firstNode();
        while (node != null) {
            if (maxLinked == null || node.getNodeValue().getAmount() > maxLinked.getAmount())
                maxLinked = node.getNodeValue();
            node = node.getNextNode();
        }
        long end1 = System.nanoTime();
        System.out.println("LinkedList - buscar máximo: " + (end1 - start1) + " ns");

        long start2 = System.nanoTime();
        PriorityQueue<Double> pq = new PriorityQueue<>();
        for (Transaction t : transactions) pq.enqueue(t.getAmount());
        Double maxPQ = pq.dequeue();
        long end2 = System.nanoTime();
        System.out.println("PriorityQueue - extraer máximo: " + (end2 - start2) + " ns");

        // 2. LinkedList vs HashTable — búsqueda de usuario por ID
        if (!users.isEmpty()) {
            String targetId = users.get(0).getId();

            long start3 = System.nanoTime();
            LinkedList<User> userList = new LinkedList<>();
            for (User u : users) userList.add(u);
            ListNode<User> userNode = userList.firstNode();
            User foundLinked = null;
            while (userNode != null) {
                if (userNode.getNodeValue().getId().equals(targetId)) {
                    foundLinked = userNode.getNodeValue();
                    break;
                }
                userNode = userNode.getNextNode();
            }
            long end3 = System.nanoTime();
            System.out.println("LinkedList - buscar usuario: " + (end3 - start3) + " ns");

            long start4 = System.nanoTime();
            HashTable<String, User> hashTable = new HashTable<>();
            for (User u : users) hashTable.put(u.getId(), u);
            User foundHash = hashTable.get(targetId);
            long end4 = System.nanoTime();
            System.out.println("HashTable - buscar usuario: " + (end4 - start4) + " ns");
        }

        System.out.println("=======================================\n");
    }
}