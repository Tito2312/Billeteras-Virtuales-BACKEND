package com.fintech.dbilleteras_virtuales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

public class ReportDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletUsageReport {
        private String walletId;
        private String walletName;
        private String walletType;
        private long transactionCount;
        private double totalAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserTransferReport {
        private String userId;
        private String userName;
        private String userEmail;
        private long transferCount;
        private double totalTransferred;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryActivityReport {
        private String category;
        private long walletCount;
        private long transactionCount;
        private double totalAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionFrequencyReport {
        private String type;
        private long count;
        private double totalAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRangeSummaryReport {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private long transactionCount;
        private double totalAmount;
        private Map<String, Double> amountByType;
    }
}
