package com.chainguard.repository;

import com.chainguard.model.Officer;
import com.chainguard.model.OfficerRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class OfficerRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Officer> ROW_MAPPER = (rs, rowNum) -> new Officer(
            rs.getInt("officer_id"),
            rs.getString("name"),
            rs.getString("rank"),
            rs.getString("department"),
            OfficerRole.fromValue(rs.getString("role")),
            rs.getString("password_hash")
    );

    public OfficerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Officer> findById(int officerId) {
        List<Officer> results = jdbc.query(
                "SELECT officer_id, name, `rank`, department, role, password_hash FROM Officer WHERE officer_id = ?",
                ROW_MAPPER,
                officerId
        );
        return results.stream().findFirst();
    }

    public List<Officer> findAll() {
        return jdbc.query(
                "SELECT officer_id, name, `rank`, department, role, '' AS password_hash FROM Officer ORDER BY officer_id DESC",
                ROW_MAPPER
        );
    }

    public int create(String name, String rank, String department, OfficerRole role, String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Officer (name, `rank`, department, role, password_hash) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setString(2, rank);
            ps.setString(3, department);
            ps.setString(4, role.getDbValue());
            ps.setString(5, passwordHash);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }
}
