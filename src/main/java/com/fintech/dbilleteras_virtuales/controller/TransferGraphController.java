package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fintech.dbilleteras_virtuales.dataStructure.GraphEdge;
import com.fintech.dbilleteras_virtuales.service.TransferGraphService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class TransferGraphController {

    private final TransferGraphService transferGraphService;

    @GetMapping("/transfers/{userId}")
    public ResponseEntity<List<GraphEdge>> getTransfersFrom(@PathVariable String userId) {
        return ResponseEntity.ok(transferGraphService.getTransfersFrom(userId));
    }

    @GetMapping("/cycles")
    public ResponseEntity<Boolean> hasCycle() {
        return ResponseEntity.ok(transferGraphService.hasCycle());
    }

    @GetMapping("/path")
    public ResponseEntity<List<String>> findPath(
        @RequestParam String sourceUserId,
        @RequestParam String targetUserId
    ) {
        return ResponseEntity.ok(transferGraphService.findPath(sourceUserId, targetUserId));
    }

    @GetMapping("/most-active")
    public ResponseEntity<String> getMostActiveUser() {
        return ResponseEntity.ok(transferGraphService.getMostActiveUser());
    }
}