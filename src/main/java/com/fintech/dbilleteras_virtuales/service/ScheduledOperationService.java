package com.fintech.dbilleteras_virtuales.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.ScheduledOperationRequestDto;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import com.fintech.dbilleteras_virtuales.repository.ScheduledOperationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledOperationService {
    
    private final  ScheduledOperationRepository scheduledOperationRepository;
    private final TransactionService transactionService;

    public ScheduledOperation createOperation(ScheduledOperationRequestDto request){
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

    public ScheduledOperation executeOperation(ScheduledOperation operation){
        try {
            switch (operation.getType()) {
                case RECHARGE:
                    transactionService.recharge(
                        operation.getUserId(),
                        operation.getTargetWalletId(),
                        operation.getAmount()
                    );
                    break;
                case WITHDRAWAL:
                    transactionService.withdrawal(
                        operation.getUserId(),
                        operation.getSourceWalletId(),
                        operation.getAmount()
                    );
                    break;
                case TRANSFER:
                    if (operation.getReceiverUserId() != null) {
                        transactionService.transfer(
                            operation.getUserId(),
                            operation.getReceiverUserId(),
                            operation.getSourceWalletId(),
                            operation.getTargetWalletId(),
                            operation.getAmount()
                        );
                    } else {
                        transactionService.transfer(
                            operation.getUserId(),
                            operation.getSourceWalletId(),
                            operation.getTargetWalletId(),
                            operation.getAmount()
                        );
                    }
                    break;
                default:
                    throw new RuntimeException("Tipo de operacion no disponible");
            }
            operation.setExecuted(true);
            return scheduledOperationRepository.save(operation);
        } catch (Exception e) {
             System.err.println("Error ejecutando operación " + operation.getId() + ": " + e.getMessage());
        }
        return operation;
    }

    @Scheduled(fixedDelay = 60000)
    public void processPending(){
        List<ScheduledOperation> pending = scheduledOperationRepository.findByExecutedFalseAndScheduledDateBefore(LocalDateTime.now());

        pending.forEach(this::executeOperation);
    }

    public List<ScheduledOperation> findByUser(String userId){
        return scheduledOperationRepository.findByUserId(userId);
    }

    public List<ScheduledOperation> findAll(){
        return scheduledOperationRepository.findAll();
    }

}
