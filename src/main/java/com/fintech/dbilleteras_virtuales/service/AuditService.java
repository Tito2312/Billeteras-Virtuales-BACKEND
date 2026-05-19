package com.fintech.dbilleteras_virtuales.service;

import org.springframework.stereotype.Service;
import com.fintech.dbilleteras_virtuales.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AuditService {

    private final TransactionService transactionService;

}
