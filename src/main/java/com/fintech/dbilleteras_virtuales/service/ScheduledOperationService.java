package com.fintech.dbilleteras_virtuales.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dto.ScheduledOperationRequestDto;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperationStatus;
import com.fintech.dbilleteras_virtuales.model.User;
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
    private final LevelBenefitService levelBenefitService;

    private static class OperationWithPriority {
        ScheduledOperation operation;
        int priority;

        OperationWithPriority(ScheduledOperation operation, int priority) {
            this.operation = operation;
            this.priority = priority;
        }
    }

    public ScheduledOperation createOperation(ScheduledOperationRequestDto request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado al crear la operación programada"));

        System.out.println("✅ Creando operación para usuario: " + user.getEmail() + " | Nivel: " + user.getLevel());

        ScheduledOperation operation = ScheduledOperation.builder()
                .userId(request.getUserId())
                .sourceWalletId(request.getSourceWalletId())
                .targetWalletId(request.getTargetWalletId())
                .transferKey(request.getTransferKey())
                .type(request.getType())
                .amount(request.getAmount())
                .scheduledDate(request.getScheduledDate())
                .executed(false)
                .status(ScheduledOperationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        ScheduledOperation saved = scheduledOperationRepository.save(operation);

        notificationService.notificationTransactionProgramadaCreada(user.getEmail());

        return saved;
    }

    public ScheduledOperation markAsFailed(String operationId, String errorMessage) {
        ScheduledOperation operation = scheduledOperationRepository.findById(operationId)
                .orElseThrow(() -> new RuntimeException("Operación programada no encontrada"));

        operation.setStatus(ScheduledOperationStatus.FAILED);
        operation.setErrorMessage(errorMessage);

        try {
            var user = userRepository.findById(operation.getUserId()).orElse(null);
            if (user != null) {
                notificationService.rejetedTransaction(user.getEmail(), "OPERACIÓN PROGRAMADA");
            } else {
                System.err.println("⚠️ Usuario no encontrado para operación fallida: " + operation.getUserId());
            }
        } catch (Exception notificationEx) {
            System.err.println("Error enviando notificación de operación fallida: " + notificationEx.getMessage());
        }

        return scheduledOperationRepository.save(operation);
    }

    public ScheduledOperation executeOperation(ScheduledOperation operation) {
        System.out.println("🚀 Ejecutando operación ID: " + operation.getId() + " | Tipo: " + operation.getType());

        try {
            User user = userRepository.findById(operation.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + operation.getUserId()));

            System.out.println("✅ Usuario encontrado: " + user.getEmail() + " | Nivel: " + user.getLevel());

            switch (operation.getType()) {
                case RECHARGE:
                    transactionService.recharge(operation.getUserId(), operation.getTargetWalletId(), operation.getAmount());
                    break;
                case WITHDRAWAL:
                    transactionService.withdrawal(operation.getUserId(), operation.getSourceWalletId(), operation.getAmount());
                    break;
                case TRANSFER:
                    if (operation.getTransferKey() != null && !operation.getTransferKey().isEmpty()) {
                        transactionService.transfer(operation.getUserId(), operation.getSourceWalletId(), operation.getTransferKey(), operation.getAmount());
                    } else if (operation.getTargetWalletId() != null) {
                        transactionService.transfer(operation.getUserId(), operation.getSourceWalletId(), operation.getTargetWalletId(), operation.getAmount());
                    } else {
                        throw new RuntimeException("No se especificó destino (transferKey o targetWalletId)");
                    }
                    break;
                default:
                    throw new RuntimeException("Tipo de operacion no disponible: " + operation.getType());
            }

            operation.setExecuted(true);
            operation.setStatus(ScheduledOperationStatus.EXECUTED);
            System.out.println("✅ Operación ejecutada exitosamente: " + operation.getId());

            notificationService.notificationTransactionProgramada(user.getEmail());

            return scheduledOperationRepository.save(operation);

        } catch (Exception e) {
            System.err.println("❌ Error ejecutando operación " + operation.getId() + ": " + e.getMessage());
            e.printStackTrace();

            operation.setStatus(ScheduledOperationStatus.FAILED);
            operation.setErrorMessage(e.getMessage());

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

    private int getPriorityByUserLevel(String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getLevel() != null) {
                    int priority = levelBenefitService.getProcessingPriority(user.getLevel());
                    System.out.println("   📊 Prioridad para usuario " + userId + " (nivel " + user.getLevel() + "): " + priority);
                    return priority;
                }
            } else {
                System.err.println("⚠️ Usuario NO encontrado en BD: " + userId);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo prioridad para usuario " + userId + ": " + e.getMessage());
        }
        return 4;
    }

    @Scheduled(fixedDelay = 30000)
    public void processPending() {
        System.out.println("\n🔄 ===== EJECUTANDO SCHEDULER DE OPERACIONES PROGRAMADAS =====");

        LocalDateTime nowColombia = LocalDateTime.now(ZoneId.of("America/Bogota"));
        LocalDateTime nowUTC = nowColombia.atZone(ZoneId.of("America/Bogota"))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        System.out.println("⏰ Hora actual Colombia: " + nowColombia);
        System.out.println("⏰ Hora actual UTC en BD: " + nowUTC);

        List<ScheduledOperation> allPending = scheduledOperationRepository.findByStatus(ScheduledOperationStatus.PENDING);
        System.out.println("📋 Total operaciones PENDING en BD: " + allPending.size());

        List<ScheduledOperation> toExecute = new ArrayList<>();
        for (ScheduledOperation op : allPending) {
            System.out.println("   🔍 Operación: " + op.getId() + " | Fecha programada: " + op.getScheduledDate());
            if (op.getScheduledDate().isBefore(nowUTC) || op.getScheduledDate().isEqual(nowUTC)) {
                toExecute.add(op);
                System.out.println("      ✅ DEBE EJECUTARSE");
            } else {
                long minutosFaltan = Duration.between(nowUTC, op.getScheduledDate()).toMinutes();
                System.out.println("      ⏳ Futura - falta: " + minutosFaltan + " minutos");
            }
        }

        if (toExecute.isEmpty()) {
            System.out.println("ℹ️ No hay operaciones vencidas para procesar\n");
            return;
        }

        List<OperationWithPriority> operationsWithPriority = new ArrayList<>();
        for (ScheduledOperation op : toExecute) {
            operationsWithPriority.add(new OperationWithPriority(op, getPriorityByUserLevel(op.getUserId())));
        }
        operationsWithPriority.sort((a, b) -> Integer.compare(a.priority, b.priority));

        System.out.println("\n📊 Operaciones ordenadas por prioridad:");
        for (OperationWithPriority owp : operationsWithPriority) {
            System.out.println("   - Operación: " + owp.operation.getId() + " | Prioridad: " + owp.priority);
        }

        for (OperationWithPriority owp : operationsWithPriority) {
            System.out.println("▶️ Ejecutando operación: " + owp.operation.getId());
            executeOperation(owp.operation);
        }

        System.out.println("✅ ===== FIN DEL PROCESAMIENTO =====\n");
    }

    public List<ScheduledOperation> findByUser(String userId) {
        return scheduledOperationRepository.findByUserId(userId);
    }

    public List<ScheduledOperation> findAll() {
        return scheduledOperationRepository.findAll();
    }

    public ScheduledOperation updateOperation(String id, ScheduledOperationRequestDto request, String userId) {
    ScheduledOperation existing = scheduledOperationRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new RuntimeException("Operación no encontrada o no autorizada"));
    if (existing.isExecuted() || existing.getStatus() != ScheduledOperationStatus.PENDING) {
        throw new RuntimeException("No se puede editar una operación ya ejecutada o fallida");
    }
    if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("La fecha no puede ser pasada");
    }
    existing.setType(request.getType());
    existing.setAmount(request.getAmount());
    existing.setScheduledDate(request.getScheduledDate());
    existing.setSourceWalletId(request.getSourceWalletId());
    existing.setTargetWalletId(request.getTargetWalletId());
    existing.setTransferKey(request.getTransferKey());
    return scheduledOperationRepository.save(existing);
}

public void deleteOperation(String id, String userId) {
    ScheduledOperation existing = scheduledOperationRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new RuntimeException("Operación no encontrada o no autorizada"));
    if (existing.isExecuted() || existing.getStatus() != ScheduledOperationStatus.PENDING) {
        throw new RuntimeException("No se puede eliminar una operación ya ejecutada o fallida");
    }
    scheduledOperationRepository.delete(existing);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {

            }
        } catch (Exception e) { }

        scheduledOperationRepository.delete(existing);
    }
}
