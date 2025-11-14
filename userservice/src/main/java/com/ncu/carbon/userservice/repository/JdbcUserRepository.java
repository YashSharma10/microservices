package com.ncu.carbon.userservice.repository;

import com.ncu.carbon.userservice.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User save(User user) {
        try {
            if (user.getId() == null) {
                jdbc.update("INSERT INTO users(name, credits, balance) VALUES(?,?,?)", 
                    user.getName(), user.getCredits(), user.getBalance());
                Long id = jdbc.queryForObject("SELECT id FROM users WHERE name = ? ORDER BY id DESC LIMIT 1", 
                    Long.class, user.getName());
                user.setId(id);
                return user;
            } else {
                jdbc.update("UPDATE users SET name = ?, credits = ?, balance = ? WHERE id = ?", 
                    user.getName(), user.getCredits(), user.getBalance(), user.getId());
                return user;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            try {
                User u = jdbc.queryForObject("SELECT id, name, credits, balance FROM users WHERE id = ?", new Object[]{id}, new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        User uu = new User();
                        uu.setId(rs.getLong("id"));
                        uu.setName(rs.getString("name"));
                        uu.setCredits(rs.getDouble("credits"));
                        uu.setBalance(rs.getDouble("balance"));
                        return uu;
                    }
                });
                return Optional.ofNullable(u);
            } catch (Exception ex) {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try {
            return jdbc.query("SELECT id, name, credits, balance FROM users ORDER BY id DESC", new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User uu = new User();
                    uu.setId(rs.getLong("id"));
                    uu.setName(rs.getString("name"));
                    uu.setCredits(rs.getDouble("credits"));
                    uu.setBalance(rs.getDouble("balance"));
                    return uu;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error finding all users: " + e.getMessage(), e);
        }
    }
}
