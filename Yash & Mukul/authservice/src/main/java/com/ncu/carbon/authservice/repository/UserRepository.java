package com.ncu.carbon.authservice.repository;

import com.ncu.carbon.authservice.model.User;

public interface UserRepository {
    boolean existsByUsername(String username);
    User save(User user);
    User findByUsername(String username);
}
