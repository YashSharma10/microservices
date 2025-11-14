package com.ncu.carbon.tradeservice.repository;

import com.ncu.carbon.tradeservice.model.Trade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcTradeRepository implements TradeRepository {

    private final JdbcTemplate jdbc;

    public JdbcTradeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Trade save(Trade t) {
        try {
            jdbc.update("INSERT INTO trades(from_user_id, to_user_id, amount) VALUES(?,?,?)",
                    t.getFromUserId(), t.getToUserId(), t.getAmount());
            Long id = jdbc.queryForObject("SELECT id FROM trades WHERE from_user_id = ? AND to_user_id = ? ORDER BY id DESC LIMIT 1",
                    Long.class, t.getFromUserId(), t.getToUserId());
            t.setId(id);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        } catch (Exception e) {
            throw new RuntimeException("Error saving trade: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Trade> findAll() {
        try {
            return jdbc.query("SELECT id, from_user_id, to_user_id, amount, created_at FROM trades ORDER BY id DESC", new TradeRowMapper());
        } catch (Exception e) {
            throw new RuntimeException("Error finding all trades: " + e.getMessage(), e);
        }
    }

    @Override
    public Trade findById(Long id) {
        try {
            try {
                return jdbc.queryForObject("SELECT id, from_user_id, to_user_id, amount, created_at FROM trades WHERE id = ?", new Object[]{id}, new TradeRowMapper());
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    static class TradeRowMapper implements RowMapper<Trade> {
        @Override
        public Trade mapRow(ResultSet rs, int rowNum) throws SQLException {
            Trade t = new Trade();
            t.setId(rs.getLong("id"));
            t.setFromUserId(rs.getLong("from_user_id"));
            t.setToUserId(rs.getLong("to_user_id"));
            t.setAmount(rs.getDouble("amount"));
            t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return t;
        }
    }
}
