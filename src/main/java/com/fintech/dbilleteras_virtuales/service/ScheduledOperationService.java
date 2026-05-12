package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.ScheduledOperationRequestDto;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperationStatus;
import com.fintech.dbilleteras_virtuales.repository.ScheduledOperationRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledOperationService {

    private final ScheduledOperationRepository scheduledOperationRepository;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ScheduledOperation createOperation(ScheduledOperationRequestDto request) {
        ScheduledOperation operation = ScheduledOperation.builder()
                .userId(request.getUserId())
                .sourceWalletId(request.getSourceWalletId())
                .targetWalletId(request.getTargetWalletId())
                .type(request.getType())
                .amount(request.getAmount())
                .scheduledDate(request.getScheduledDate())
                .executed(false)
                .createdAt(LocalDateTime.now())
                .build();

        return scheduledOperationRepository.save(operation);
    }

    public ScheduledOperation markAsFailed(String operationId, String errorMessage) {
        ScheduledOperation operation = scheduledOperationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operación programada no encontrada"));

        operation.setStatus(ScheduledOperationStatus.FAILED);
        operation.setErrorMessage(errorMessage);

        // Enviar notificación de operación fallida
        try {
            var user = userRepository.findById(operation.getUserId()).orElse(null);
            if (user != null) {
                notificationService.rejetedTransaction(user.getEmail(), "OPERACIÓN PROGRAMADA");
            }
        } catch (Exception notificationEx) {
            System.err.println("Error enviando notificación de operación fallida: " + notificationEx.getMessage());
        }

        return scheduledOperationRepository.save(operation);
    }

    public ScheduledOperation executeOperation(ScheduledOperation operation) {
        try {
            switch (operation.getType()) {
                case RECHARGE:
                    transactionService.recharge(
                            operation.getUserId(),
                            operation.getTargetWalletId(),
                            operation.getAmount());
                    break;
                case WITHDRAWAL:
                    transactionService.withdrawal(
                            operation.getUserId(),
                            operation.getSourceWalletId(),
                            operation.getAmount());
                    break;
                case TRANSFER:
                    if (operation.getReceiverUserId() != null) {
                        transactionService.transfer(
                                operation.getUserId(),
                                operation.getReceiverUserId(),
                                operation.getSourceWalletId(),
                                operation.getTargetWalletId(),
                                operation.getAmount());
                    } else {
                        transactionService.transfer(
                                operation.getUserId(),
                                operation.getSourceWalletId(),
                                operation.getTargetWalletId(),
                                operation.getAmount());
                    }
                    break;
                default:
                    throw new RuntimeException("Tipo de operacion no disponible");
            }
            operation.setExecuted(true);
            return scheduledOperationRepository.save(operation);
        } catch (Exception e) {
            System.err.println("Error ejecutando operación " + operation.getId() + ": " + e.getMessage());

            // Marcar operación como fallida
            operation.setStatus(ScheduledOperationStatus.FAILED);
            operation.setErrorMessage(e.getMessage());

            // Enviar notificación de rechazo
            try {
                var user = userRepository.findById(operation.getUserId()).orElse(null);
                if (user != null) {
                    notificationService.rejetedTransaction(user.getEmail(), "OPERACIÓN PROGRAMADA");
                }
            } catch (Exception notificationEx) {
                System.err.println("Error enviando notificación de rechazo: " + notificationEx.getMessage());
            }

            return scheduledOperationRepository.save(operation);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void processPending() {
        List<ScheduledOperation> pending = scheduledOperationRepository
                .findByExecutedFalseAndScheduledDateBefore(LocalDateTime.now());

        pending.forEach(this::executeOperation);
    }

    public List<ScheduledOperation> findByUser(String userId) {
        return scheduledOperationRepository.findByUserId(userId);
    }

    public List<ScheduledOperation> findAll() {
        return scheduledOperationRepository.findAll();
    }

}
