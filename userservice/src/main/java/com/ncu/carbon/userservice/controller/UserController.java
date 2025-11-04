package com.ncu.carbon.userservice.controller;

import com.ncu.carbon.userservice.dto.UserDto;
import com.ncu.carbon.userservice.model.User;
import com.ncu.carbon.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        User saved = userService.createUser(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> u = userService.getUser(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/addCredits")
    public ResponseEntity<User> addCredits(@PathVariable Long id, @RequestParam double amount) {
        Optional<User> ou = userService.addCredits(id, amount);
        return ou.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/removeCredits")
    public ResponseEntity<User> removeCredits(@PathVariable Long id, @RequestParam double amount) {
        Optional<User> ou = userService.removeCredits(id, amount);
        if (ou.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ou.get());
    }
}
