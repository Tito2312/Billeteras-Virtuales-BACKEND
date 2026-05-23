package com.fintech.dbilleteras_virtuales.service;

import com.fintech.dbilleteras_virtuales.dataStructure.LinkedList;
import com.fintech.dbilleteras_virtuales.dataStructure.ListNode;
import com.fintech.dbilleteras_virtuales.dataStructure.PriorityQueue;
import com.fintech.dbilleteras_virtuales.dto.ReportDto;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    private static class EntryCount {
        String key;
        long count;
        double totalAmount;

        EntryCount(String key) {
            this.key = key;
            this.count = 0;
            this.totalAmount = 0;
        }
    }

    private EntryCount getOrCreate(LinkedList<EntryCount> list, String key) {
        ListNode<EntryCount> node = list.firstNode();
        while (node != null) {
            if (node.getNodeValue().key.equals(key)) {
                return node.getNodeValue();
            }
            node = node.getNextNode();
        }
        EntryCount newEntry = new EntryCount(key);
        list.add(newEntry);
        return newEntry;
    }

    private void sortByCountDesc(LinkedList<EntryCount> list) {
        if (list.getSize() <= 1)
            return;
        boolean swapped;
        do {
            swapped = false;
            ListNode<EntryCount> node = list.firstNode();
            while (node != null && node.getNextNode() != null) {
                EntryCount current = node.getNodeValue();
                EntryCount next = node.getNextNode().getNodeValue();
                if (current.count < next.count) {
                    node.setNodeValue(next);
                    node.getNextNode().setNodeValue(current);
                    swapped = true;
                }
                node = node.getNextNode();
            }
        } while (swapped);
    }

    public List<ReportDto.WalletUsageReport> getWalletsWithMostUsage(int topN) {
        List<Transaction> all = transactionRepository.findAll();

        LinkedList<Transaction> txList = new LinkedList<>();
        for (Transaction t : all)
            txList.add(t);

        LinkedList<EntryCount> count = new LinkedList<>();

        ListNode<Transaction> node = txList.firstNode();
        while (node != null) {
            Transaction t = node.getNodeValue();
            if (t.getSourceWallet() != null) {
                EntryCount entry = getOrCreate(count, t.getSourceWallet());
                entry.count++;
                entry.totalAmount += t.getAmount();
            }
            if (t.getTargetWallet() != null && !t.getTargetWallet().equals(t.getSourceWallet())) {
                EntryCount entry = getOrCreate(count, t.getTargetWallet());
                entry.count++;
            }
            node = node.getNextNode();
        }

        sortByCountDesc(count);

        List<ReportDto.WalletUsageReport> result = new ArrayList<>();
        ListNode<EntryCount> nodeC = count.firstNode();
        int i = 0;
        while (nodeC != null && i < topN) {
            EntryCount p = nodeC.getNodeValue();
            Optional<Wallet> wallet = walletRepository.findById(p.key);
            result.add(ReportDto.WalletUsageReport.builder()
                    .walletId(p.key)
                    .userId(wallet.map(Wallet::getUserId).orElse(null))
                    .walletName(wallet.map(Wallet::getName).orElse("Unknown"))
                    .walletType(wallet.map(Wallet::getType).orElse("Unknown"))
                    .transactionCount(p.count)
                    .totalAmount(p.totalAmount)
                    .build());
            nodeC = nodeC.getNextNode();
            i++;
        }
        return result;
    }

    public List<ReportDto.UserTransferReport> getUsersWithMostTransfers(int topN, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transfers = (start != null && end != null)
                ? transactionRepository.findByTypeAndCreatedAtBetween(TransactionType.TRANSFER, start, end)
                : transactionRepository.findByType(TransactionType.TRANSFER);

        LinkedList<Transaction> txList = new LinkedList<>();
        for (Transaction t : transfers)
            txList.add(t);

        LinkedList<EntryCount> count = new LinkedList<>();

        ListNode<Transaction> node = txList.firstNode();
        while (node != null) {
            Transaction t = node.getNodeValue();
            EntryCount entry = getOrCreate(count, t.getUserId());
            entry.count++;
            entry.totalAmount += t.getAmount();
            node = node.getNextNode();
        }

        sortByCountDesc(count);

        List<ReportDto.UserTransferReport> result = new ArrayList<>();
        ListNode<EntryCount> nodeC = count.firstNode();
        int i = 0;
        while (nodeC != null && i < topN) {
            EntryCount p = nodeC.getNodeValue();
            userRepository.findById(p.key).ifPresent(user -> result.add(ReportDto.UserTransferReport.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .userEmail(user.getEmail())
                    .transferCount(p.count)
                    .totalTransferred(p.totalAmount)
                    .build()));
            nodeC = nodeC.getNextNode();
            i++;
        }
        return result;
    }

    public List<ReportDto.CategoryActivityReport> getMostActiveWalletCategories() {
        List<Wallet> allWallets = walletRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();

        LinkedList<Wallet> walletList = new LinkedList<>();
        for (Wallet w : allWallets)
            walletList.add(w);

        LinkedList<Transaction> txList = new LinkedList<>();
        for (Transaction t : allTransactions)
            txList.add(t);

        LinkedList<EntryCount> categoryCount = new LinkedList<>();

        ListNode<Transaction> nodeT = txList.firstNode();
        while (nodeT != null) {
            Transaction t = nodeT.getNodeValue();
            String type = resolveWalletType(walletList, t.getSourceWallet());
            EntryCount entry = getOrCreate(categoryCount, type);
            entry.count++;
            entry.totalAmount += t.getAmount();
            nodeT = nodeT.getNextNode();
        }

        LinkedList<EntryCount> walletCount = new LinkedList<>();
        ListNode<Wallet> nodeW = walletList.firstNode();
        while (nodeW != null) {
            Wallet w = nodeW.getNodeValue();
            EntryCount entry = getOrCreate(walletCount, w.getType());
            entry.count++;
            nodeW = nodeW.getNextNode();
        }

        sortByCountDesc(categoryCount);

        List<ReportDto.CategoryActivityReport> result = new ArrayList<>();
        ListNode<EntryCount> nodeC = categoryCount.firstNode();
        while (nodeC != null) {
            EntryCount p = nodeC.getNodeValue();
            long walletCountVal = searchCount(walletCount, p.key);
            result.add(ReportDto.CategoryActivityReport.builder()
                    .category(p.key)
                    .transactionCount(p.count)
                    .walletCount(walletCountVal)
                    .totalAmount(p.totalAmount)
                    .build());
            nodeC = nodeC.getNextNode();
        }
        return result;
    }

    public List<ReportDto.TransactionFrequencyReport> getTransactionFrequencyByType() {
        List<Transaction> all = transactionRepository.findAll();

        LinkedList<Transaction> txList = new LinkedList<>();
        for (Transaction t : all)
            txList.add(t);

        LinkedList<EntryCount> count = new LinkedList<>();

        ListNode<Transaction> node = txList.firstNode();
        while (node != null) {
            Transaction t = node.getNodeValue();
            EntryCount entry = getOrCreate(count, t.getType().name());
            entry.count++;
            entry.totalAmount += t.getAmount();
            node = node.getNextNode();
        }

        sortByCountDesc(count);

        List<ReportDto.TransactionFrequencyReport> result = new ArrayList<>();
        ListNode<EntryCount> nodeC = count.firstNode();
        while (nodeC != null) {
            EntryCount p = nodeC.getNodeValue();
            result.add(ReportDto.TransactionFrequencyReport.builder()
                    .type(p.key)
                    .count(p.count)
                    .totalAmount(p.totalAmount)
                    .build());
            nodeC = nodeC.getNextNode();
        }
        return result;
    }

    public ReportDto.DateRangeSummaryReport getTotalAmountInDateRange(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(start, end);

        LinkedList<Transaction> txList = new LinkedList<>();
        for (Transaction t : transactions)
            txList.add(t);

        double totalAmount = 0;
        LinkedList<EntryCount> typeCount = new LinkedList<>();

        ListNode<Transaction> node = txList.firstNode();
        while (node != null) {
            Transaction t = node.getNodeValue();
            totalAmount += t.getAmount();
            EntryCount entry = getOrCreate(typeCount, t.getType().name());
            entry.totalAmount += t.getAmount();
            node = node.getNextNode();
        }

        Map<String, Double> amountByType = new HashMap<>();
        ListNode<EntryCount> nodeC = typeCount.firstNode();
        while (nodeC != null) {
            amountByType.put(nodeC.getNodeValue().key, nodeC.getNodeValue().totalAmount);
            nodeC = nodeC.getNextNode();
        }

        return ReportDto.DateRangeSummaryReport.builder()
                .startDate(start)
                .endDate(end)
                .transactionCount(txList.getSize())
                .totalAmount(totalAmount)
                .amountByType(amountByType)
                .build();
    }

    public List<Transaction> getTopTransactionsByAmount(int topN) {
        List<Transaction> all = transactionRepository.findAll();

        // PriorityQueue propia ordenada descendente por monto (via Comparable)
        PriorityQueue<Transaction> pq = new PriorityQueue<>();
        for (Transaction t : all) {
            pq.enqueue(t);
        }

        List<Transaction> result = new ArrayList<>();
        int i = 0;
        while (!pq.isEmpty() && i < topN) {
            result.add(pq.dequeue());
            i++;
        }
        return result;
    }

    private String resolveWalletType(LinkedList<Wallet> walletList, String walletId) {
        if (walletId == null)
            return "Unknown";
        ListNode<Wallet> node = walletList.firstNode();
        while (node != null) {
            if (node.getNodeValue().getId().equals(walletId)) {
                return node.getNodeValue().getType();
            }
            node = node.getNextNode();
        }
        return "Unknown";
    }

    private long searchCount(LinkedList<EntryCount> list, String key) {
        ListNode<EntryCount> node = list.firstNode();
        while (node != null) {
            if (node.getNodeValue().key.equals(key)) {
                return node.getNodeValue().count;
            }
            node = node.getNextNode();
        }
        return 0;
    }
}
