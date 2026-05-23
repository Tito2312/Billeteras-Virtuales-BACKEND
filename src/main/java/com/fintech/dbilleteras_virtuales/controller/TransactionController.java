package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAll() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @PostMapping("/recharge")
    public ResponseEntity<Transaction> recharge(@RequestParam String userId, @RequestParam String targetWallet,
            @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.recharge(userId, targetWallet, amount));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Transaction> withdrawal(@RequestParam String userId, @RequestParam String sourceWallet,
            @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.withdrawal(userId, sourceWallet, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestParam String userId, @RequestParam String sourceWallet,
            @RequestParam String targetWallet, @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.transfer(userId, sourceWallet, targetWallet, amount));
    }

    @PostMapping("transactiosDateRange")
    public ResponseEntity<List<Transaction>> dateRange(@RequestParam String walletId, @RequestParam String dateFirst,
            @RequestParam String dateLast) {

        return ResponseEntity.ok(transactionService.getTransactionsByWalletAndDateRange(walletId, dateFirst, dateLast));
    }

    @PostMapping("/transfer-to-user")
    public ResponseEntity<Transaction> transferToUser(@RequestParam String userId,
            @RequestParam String sourceWallet, @RequestParam String transferkey, @RequestParam double amount) {
        return ResponseEntity
                .ok(transactionService.transfer(userId, sourceWallet, transferkey, amount));
    }

    @PostMapping("historyTransactions")
    public ResponseEntity<List<Transaction>> historyTransactions(@RequestBody String userId) {

        return ResponseEntity.ok(transactionService.ListTransactions(userId));
    }

    @PutMapping("/wallet-transaction")
    public ResponseEntity<List<Transaction>> wallettransaction(@RequestParam String walletId) {

        return ResponseEntity.ok(transactionService.listWalletsTransactions(walletId));
    }

    @PutMapping("/reverseTransaction")
    public ResponseEntity<Transaction> reverseTransaction(@RequestParam String userId,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(transactionService.reverseTransactionList(userId, transactionId));
    }

    @PutMapping("/reverse-pila")
    public ResponseEntity<?> revertirConPila(@RequestParam String userId) {

        return ResponseEntity.ok(transactionService.reverseTransactionPila(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getHistoryByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(transactionService.getHistoryByUserId(userId));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<Transaction>> getHistoryByWalletId(@PathVariable String walletId) {
        return ResponseEntity.ok(transactionService.getHistoryByWalletId(walletId));
    }

    @GetMapping("/queeue")
    public ResponseEntity HistoryQueueReverseTransactions(@RequestParam String userId) {
        return ResponseEntity.ok(transactionService.historyQueue(userId).toList());
    }

}
