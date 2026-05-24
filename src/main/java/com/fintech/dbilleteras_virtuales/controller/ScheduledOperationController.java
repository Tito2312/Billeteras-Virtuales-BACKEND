package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.dbilleteras_virtuales.dto.ScheduledOperationRequestDto;
import com.fintech.dbilleteras_virtuales.model.ScheduledOperation;
import com.fintech.dbilleteras_virtuales.service.ScheduledOperationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scheduledOperation")
@RequiredArgsConstructor
public class ScheduledOperationController {

    private final ScheduledOperationService scheduledOperationService;

    @PostMapping
    public ResponseEntity<ScheduledOperation> create(@RequestBody @Valid ScheduledOperationRequestDto request){
        return ResponseEntity.ok(scheduledOperationService.createOperation(request));
    }

    @PostMapping("/executeOperation/{id}")
    public ResponseEntity<ScheduledOperation> execute(@PathVariable String id, @RequestBody @Valid ScheduledOperation operation){

        return ResponseEntity.ok(scheduledOperationService.executeOperation(operation));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ScheduledOperation>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(scheduledOperationService.findByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<ScheduledOperation>> findAll() {
        return ResponseEntity.ok(scheduledOperationService.findAll());
    }

   @PutMapping("/{id}")
public ResponseEntity<ScheduledOperation> update(
        @PathVariable String id,
        @Valid @RequestBody ScheduledOperationRequestDto request,
        @RequestHeader("userId") String userId) {
    return ResponseEntity.ok(scheduledOperationService.updateOperation(id, request, userId));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(
        @PathVariable String id,
        @RequestHeader("userId") String userId) {
    scheduledOperationService.deleteOperation(id, userId);
    return ResponseEntity.noContent().build();
}
}
