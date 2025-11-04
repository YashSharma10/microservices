package com.ncu.carbon.carbonservice.repository;

import com.ncu.carbon.carbonservice.model.Carbon;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcCarbonRepository implements CarbonRepository {

    private final JdbcTemplate jdbc;

    public JdbcCarbonRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Carbon> mapper = new RowMapper<Carbon>() {
        @Override
        public Carbon mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Carbon(
                rs.getLong("id"), 
                rs.getString("name"), 
                rs.getDouble("supply"),
                rs.getLong("owner_id"),
                rs.getDouble("price")
            );
        }
    };

    @Override
    public List<Carbon> findAll() {
        return jdbc.query("SELECT id, name, supply, owner_id, price FROM carbon", mapper);
    }

    @Override
    public Carbon findById(Long id) {
        try {
            return jdbc.queryForObject("SELECT id, name, supply, owner_id, price FROM carbon WHERE id = ?", new Object[]{id}, mapper);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Carbon save(Carbon carbon) {
        jdbc.update("INSERT INTO carbon(name, supply, owner_id, price) VALUES(?,?,?,?)", 
            carbon.getName(), carbon.getSupply(), carbon.getOwnerId(), carbon.getPrice());
        Long id = jdbc.queryForObject("SELECT id FROM carbon WHERE name = ? AND owner_id = ? ORDER BY id DESC LIMIT 1", 
            Long.class, carbon.getName(), carbon.getOwnerId());
        carbon.setId(id);
        return carbon;
    }

    @Override
    public boolean update(Carbon carbon) {
        int updated = jdbc.update("UPDATE carbon SET name = ?, supply = ?, price = ? WHERE id = ?", 
            carbon.getName(), carbon.getSupply(), carbon.getPrice(), carbon.getId());
        return updated > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        int deleted = jdbc.update("DELETE FROM carbon WHERE id = ?", id);
        return deleted > 0;
    }
}
