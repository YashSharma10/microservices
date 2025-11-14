package com.ncu.carbon.authservice.repository;

import com.ncu.carbon.authservice.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User save(User user) {
        try {
            jdbc.update("INSERT INTO users(username, password) VALUES(?,?)", user.getUsername(), user.getPassword());
            Long id = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, user.getUsername());
            user.setId(id);
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findByUsername(String username) {
        try {
            try {
                return jdbc.queryForObject("SELECT id, username, password FROM users WHERE username = ?", new Object[]{username}, new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"));
                    }
                });
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
