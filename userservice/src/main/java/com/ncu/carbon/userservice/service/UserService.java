package com.ncu.carbon.userservice.service;

import com.ncu.carbon.userservice.dto.UserDto;
import com.ncu.carbon.userservice.model.User;
import com.ncu.carbon.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(UserDto dto) {
        try {
            // Default credits to 100 if not specified
            double credits = dto.getCredits() != null ? dto.getCredits() : 100.0;
            User u = new User(dto.getName(), credits, 0.0);
            return repository.save(u);
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    public Optional<User> getUser(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> addCredits(Long id, double amount) {
        try {
            Optional<User> ou = repository.findById(id);
            if (ou.isEmpty()) return Optional.empty();
            User u = ou.get();
            u.setCredits(u.getCredits() + amount);
            repository.save(u);
            return Optional.of(u);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> removeCredits(Long id, double amount) {
        try {
            Optional<User> ou = repository.findById(id);
            if (ou.isEmpty()) return Optional.empty();
            User u = ou.get();
            if (u.getCredits() < amount) return Optional.empty();
            u.setCredits(u.getCredits() - amount);
            repository.save(u);
            return Optional.of(u);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> addBalance(Long id, double amount) {
        try {
            Optional<User> ou = repository.findById(id);
            if (ou.isEmpty()) return Optional.empty();
            User u = ou.get();
            u.setBalance(u.getBalance() + amount);
            repository.save(u);
            return Optional.of(u);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> removeBalance(Long id, double amount) {
        try {
            Optional<User> ou = repository.findById(id);
            if (ou.isEmpty()) return Optional.empty();
            User u = ou.get();
            if (u.getBalance() < amount) return Optional.empty();
            u.setBalance(u.getBalance() - amount);
            repository.save(u);
            return Optional.of(u);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
