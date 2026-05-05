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
    public ResponseEntity<Transaction> recharge(@RequestParam String userId, @RequestParam String targetWallet, @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.recharge(userId, targetWallet, amount));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Transaction> withdrawal(@RequestParam String userId, @RequestParam String sourceWallet, @RequestParam double amount){
        return ResponseEntity.ok(transactionService.withdrawal(userId, sourceWallet, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(@RequestParam String userId, @RequestParam String sourceWallet, @RequestParam String targetWallet, @RequestParam double amount){
        return ResponseEntity.ok(transactionService.transfer(userId, sourceWallet, targetWallet, amount));
    }

    @PutMapping("/reverseTransaction")
    public ResponseEntity<Transaction> reverseTransaction(@RequestParam String transactionId){
        return ResponseEntity.ok(transactionService.reverseTransaction(transactionId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Transaction>> getHistoryByUserId(@RequestParam String userId){
        return ResponseEntity.ok(transactionService.getHistoryByUserId(userId));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<List<Transaction>> getHistoryByWalletId(@RequestParam String walletId){
        return ResponseEntity.ok(transactionService.getHistoryByWalletId(walletId));
    }
}
