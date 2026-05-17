package com.fintech.dbilleteras_virtuales.controller;

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

    // GET /api/reports/wallets/most-used?top=5
    @GetMapping("/wallets/most-used")
    public ResponseEntity<List<ReportDto.WalletUsageReport>> getWalletsWithMostUsage(
            @RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(reportService.getWalletsWithMostUsage(top));
    }

    // GET /api/reports/users/most-transfers?top=5
    @GetMapping("/users/most-transfers")
    public ResponseEntity<List<ReportDto.UserTransferReport>> getUsersWithMostTransfers(
            @RequestParam(defaultValue = "5") int top) {
        return ResponseEntity.ok(reportService.getUsersWithMostTransfers(top));
    }

    // GET /api/reports/wallets/categories
    @GetMapping("/wallets/categories")
    public ResponseEntity<List<ReportDto.CategoryActivityReport>> getMostActiveWalletCategories() {
        return ResponseEntity.ok(reportService.getMostActiveWalletCategories());
    }

    // GET /api/reports/transactions/frequency
    @GetMapping("/transactions/frequency")
    public ResponseEntity<List<ReportDto.TransactionFrequencyReport>> getTransactionFrequencyByType() {
        return ResponseEntity.ok(reportService.getTransactionFrequencyByType());
    }

    // GET /api/reports/transactions/date-range?start=2025-01-01T00:00:00&end=2025-12-31T23:59:59
    @GetMapping("/transactions/date-range")
    public ResponseEntity<ReportDto.DateRangeSummaryReport> getTotalAmountInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getTotalAmountInDateRange(start, end));
    }
}
