package com.ncu.carbon.authservice.service;

import com.ncu.carbon.authservice.model.User;
import com.ncu.carbon.authservice.repository.UserRepository;
import com.ncu.carbon.authservice.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository repository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public boolean createUser(String username, String rawPassword) {
        try {
            if (repository.existsByUsername(username)) return false;
            String hashed = passwordEncoder.encode(rawPassword);
            User u = new User(null, username, hashed);
            repository.save(u);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String loginAndCreateToken(String username, String rawPassword) {
        try {
            User u = repository.findByUsername(username);
            if (u == null) return null;
            if (passwordEncoder.matches(rawPassword, u.getPassword())) {
                return jwtUtil.generateToken(username);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateCredentials(String username, String rawPassword) {
        try {
            User u = repository.findByUsername(username);
            if (u == null) return false;
            return passwordEncoder.matches(rawPassword, u.getPassword());
        } catch (Exception e) {
            return false;
        }
    }
}
