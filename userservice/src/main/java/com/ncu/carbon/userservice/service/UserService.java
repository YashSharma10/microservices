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
        User u = new User(dto.getName(), dto.getCredits());
        return repository.save(u);
    }

    public Optional<User> getUser(Long id) {
        return repository.findById(id);
    }

    public Optional<User> addCredits(Long id, double amount) {
        Optional<User> ou = repository.findById(id);
        if (ou.isEmpty()) return Optional.empty();
        User u = ou.get();
        u.setCredits(u.getCredits() + amount);
        repository.save(u);
        return Optional.of(u);
    }

    public Optional<User> removeCredits(Long id, double amount) {
        Optional<User> ou = repository.findById(id);
        if (ou.isEmpty()) return Optional.empty();
        User u = ou.get();
        if (u.getCredits() < amount) return Optional.empty();
        u.setCredits(u.getCredits() - amount);
        repository.save(u);
        return Optional.of(u);
    }
}
