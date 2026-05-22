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

    // Clase auxiliar para guardar operación con su prioridad
    private static class OperationWithPriority {
        ScheduledOperation operation;
        int priority;
        
        OperationWithPriority(ScheduledOperation operation, int priority) {
            this.operation = operation;
            this.priority = priority;
        }
    }

    public ScheduledOperation createOperation(ScheduledOperationRequestDto request) {
        // Validar que el usuario existe ANTES de crear
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

        return scheduledOperationRepository.save(operation);
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
            // Verificar que el usuario existe ANTES de ejecutar
            User user = userRepository.findById(operation.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + operation.getUserId()));
            
            System.out.println("✅ Usuario encontrado: " + user.getEmail() + " | Nivel: " + user.getLevel());
            
            switch (operation.getType()) {
                case RECHARGE:
                    System.out.println("📥 Ejecutando recarga programada de $" + operation.getAmount() + " a wallet: " + operation.getTargetWalletId());
                    transactionService.recharge(
                            operation.getUserId(),
                            operation.getTargetWalletId(),
                            operation.getAmount());
                    break;
                case WITHDRAWAL:
                    System.out.println("📤 Ejecutando retiro programado de $" + operation.getAmount() + " desde wallet: " + operation.getSourceWalletId());
                    transactionService.withdrawal(
                            operation.getUserId(),
                            operation.getSourceWalletId(),
                            operation.getAmount());
                    break;
                case TRANSFER:
                    System.out.println("🔄 Ejecutando transferencia programada de $" + operation.getAmount());
                    if (operation.getTransferKey() != null && !operation.getTransferKey().isEmpty()) {
                        System.out.println("   → A billetera con clave: " + operation.getTransferKey());
                        transactionService.transfer(
                                operation.getUserId(),
                                operation.getSourceWalletId(),
                                operation.getTransferKey(),
                                operation.getAmount());
                    } else if (operation.getTargetWalletId() != null) {
                        System.out.println("   → A wallet propia: " + operation.getTargetWalletId());
                        transactionService.transfer(
                                operation.getUserId(),
                                operation.getSourceWalletId(),
                                operation.getTargetWalletId(),
                                operation.getAmount());
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

    // Obtener prioridad según el nivel del usuario
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
        
        // Obtener hora actual en Colombia
        LocalDateTime nowColombia = LocalDateTime.now(ZoneId.of("America/Bogota"));
        // Convertir a UTC para comparar con la BD
        LocalDateTime nowUTC = nowColombia.atZone(ZoneId.of("America/Bogota"))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        
        System.out.println("⏰ Hora actual Colombia: " + nowColombia);
        System.out.println("⏰ Hora actual UTC en BD: " + nowUTC);
        
        // Buscar operaciones PENDING
        List<ScheduledOperation> allPending = scheduledOperationRepository.findByStatus(ScheduledOperationStatus.PENDING);
        
        System.out.println("📋 Total operaciones PENDING en BD: " + allPending.size());
        
        // Filtrar manualmente las que deberían ejecutarse
        List<ScheduledOperation> toExecute = new ArrayList<>();
        for (ScheduledOperation op : allPending) {
            System.out.println("   🔍 Operación: " + op.getId() + 
                             " | Fecha programada (guardada): " + op.getScheduledDate() +
                             " | Tipo: " + op.getType());
            
            // Comparar fechas correctamente
            if (op.getScheduledDate().isBefore(nowUTC) || op.getScheduledDate().isEqual(nowUTC)) {
                toExecute.add(op);
                System.out.println("      ✅ DEBE EJECUTARSE (fecha pasada o igual)");
            } else {
                long minutosFaltan = Duration.between(nowUTC, op.getScheduledDate()).toMinutes();
                System.out.println("      ⏳ Futura - falta: " + minutosFaltan + " minutos");
            }
        }
        
        System.out.println("📋 Operaciones a ejecutar: " + toExecute.size());
        
        if (toExecute.isEmpty()) {
            System.out.println("ℹ️ No hay operaciones vencidas para procesar\n");
            return;
        }
        
        // Crear lista con prioridad
        List<OperationWithPriority> operationsWithPriority = new ArrayList<>();
        for (ScheduledOperation op : toExecute) {
            int priority = getPriorityByUserLevel(op.getUserId());
            operationsWithPriority.add(new OperationWithPriority(op, priority));
        }
        
        operationsWithPriority.sort((a, b) -> Integer.compare(a.priority, b.priority));
        
        System.out.println("\n📊 Operaciones ordenadas por prioridad:");
        for (OperationWithPriority owp : operationsWithPriority) {
            System.out.println("   - Operación: " + owp.operation.getId() + 
                             " | Prioridad: " + owp.priority + 
                             " | Tipo: " + owp.operation.getType());
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
}