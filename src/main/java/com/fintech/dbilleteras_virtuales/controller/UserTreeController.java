package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.fintech.dbilleteras_virtuales.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.fintech.dbilleteras_virtuales.service.UserTreeService;

@RestController
@RequestMapping("/api/tree")
@RequiredArgsConstructor
public class UserTreeController {

    private final UserTreeService userTreeService;

    @GetMapping("/users/ordered")
    public ResponseEntity<List<User>> getOrderedUsers() {
        return ResponseEntity.ok(userTreeService.getOrderedUsers());
    }

    @GetMapping("/users/range")
    public ResponseEntity<List<User>> getUsersByRange(@RequestParam int min, @RequestParam int max) {
        return ResponseEntity.ok(userTreeService.getUsersByRange(min, max));
    }

    @GetMapping("/users/top")
    public ResponseEntity<User> getTopUser() {
        return ResponseEntity.ok(userTreeService.getTopUser());
    }

    @GetMapping("/users/level/{level}")
    public ResponseEntity<List<User>> getUsersByLevel(@PathVariable String level) {
        return ResponseEntity.ok(userTreeService.getUsersByLevel(level));
    }
}
