package com.fintech.dbilleteras_virtuales.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.dbilleteras_virtuales.dto.UpdatedUserDto;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

   @PutMapping("/{id}")
public ResponseEntity<User> updateUser(
        @PathVariable String id,
        @RequestBody @Valid UpdatedUserDto updatedUser,
        @AuthenticationPrincipal UserDetails currentUser) {

    String tokenEmail = currentUser.getUsername().trim().toLowerCase();
    String requestEmail = updatedUser.getEmail().trim().toLowerCase();

    if (!tokenEmail.equals(requestEmail)) {
        return ResponseEntity.status(403).body(null);
    }

    User user = userService.findById(id);
    user.setName(updatedUser.getName());
    user.setPhoneNumber(updatedUser.getPhoneNumber());
    user.setEmail(updatedUser.getEmail());

    return ResponseEntity.ok(userService.updateUser(id, user));
}

    @PatchMapping("/{id}/activate")
    public ResponseEntity<User> activate(@PathVariable String id) {
        return ResponseEntity.ok(userService.activate(id));
    }

    @PatchMapping("/{id}/desactivate")
    public ResponseEntity<User> desactivate(@PathVariable String id) {
        return ResponseEntity.ok(userService.desactivate(id));
    }
}
