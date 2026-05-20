package com.fintech.dbilleteras_virtuales.controller;

import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.dto.ReportDto;
import com.fintech.dbilleteras_virtuales.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/wallets/most-used")
    public ResponseEntity<List<ReportDto.WalletUsageReport>> getWalletsWithMostUsage(
            @RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(reportService.getWalletsWithMostUsage(top));
    }

    @GetMapping("/users/most-transfers")
    public ResponseEntity<List<ReportDto.UserTransferReport>> getUsersWithMostTransfers(
            @RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(reportService.getUsersWithMostTransfers(top));
    }

    @GetMapping("/wallets/categories")
    public ResponseEntity<List<ReportDto.CategoryActivityReport>> getMostActiveWalletCategories() {
        return ResponseEntity.ok(reportService.getMostActiveWalletCategories());
    }

    @GetMapping("/transactions/frequency")
    public ResponseEntity<List<ReportDto.TransactionFrequencyReport>> getTransactionFrequencyByType() {
        return ResponseEntity.ok(reportService.getTransactionFrequencyByType());
    }

    @GetMapping("/transactions/date-range")
    public ResponseEntity<ReportDto.DateRangeSummaryReport> getTotalAmountInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getTotalAmountInDateRange(start, end));
    }

    @GetMapping("/transactions/top-amount")
    public ResponseEntity<List<Transaction>> getTopTransactionsByAmount(
            @RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(reportService.getTopTransactionsByAmount(top));
    }
}
