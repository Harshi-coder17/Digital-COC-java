package com.chainguard.repository;

import com.chainguard.model.Evidence;
import com.chainguard.model.EvidenceStatus;
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
public class EvidenceRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Evidence> ROW_MAPPER = (rs, rowNum) -> new Evidence(
            rs.getInt("evidence_id"),
            rs.getInt("case_id"),
            rs.getString("case_title"),
            rs.getString("type"),
            rs.getString("hash_value"),
            EvidenceStatus.fromValue(rs.getString("status")),
            rs.getString("storage_location")
    );

    public EvidenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Evidence> findAll(Integer caseId) {
        String baseSql = """
                SELECT e.evidence_id, e.case_id, c.case_title, e.type, e.hash_value, e.status,
                       s.location AS storage_location
                FROM Evidence e
                JOIN Case_Details c ON e.case_id = c.case_id
                LEFT JOIN Storage s ON e.evidence_id = s.evidence_id
                """;
        if (caseId != null) {
            return jdbc.query(baseSql + " WHERE e.case_id = ? ORDER BY e.evidence_id DESC", ROW_MAPPER, caseId);
        }
        return jdbc.query(baseSql + " ORDER BY e.evidence_id DESC", ROW_MAPPER);
    }

    public Optional<Evidence> findById(int evidenceId) {
        String sql = """
                SELECT e.evidence_id, e.case_id, c.case_title, e.type, e.hash_value, e.status,
                       s.location AS storage_location
                FROM Evidence e
                JOIN Case_Details c ON e.case_id = c.case_id
                LEFT JOIN Storage s ON e.evidence_id = s.evidence_id
                WHERE e.evidence_id = ?
                """;
        return jdbc.query(sql, ROW_MAPPER, evidenceId).stream().findFirst();
    }

    public int create(int caseId, String type, String hashValue, EvidenceStatus status, String storageLocation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Evidence (case_id, type, hash_value, status) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, caseId);
            ps.setString(2, type);
            ps.setString(3, hashValue);
            ps.setString(4, status.getDbValue());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        int evidenceId = key != null ? key.intValue() : 0;

        if (evidenceId > 0 && storageLocation != null && !storageLocation.isBlank()) {
            jdbc.update(
                    "INSERT INTO Storage (evidence_id, location) VALUES (?, ?)",
                    evidenceId, storageLocation.trim()
            );
        }
        return evidenceId;
    }

    /** Calls MySQL stored function GetCustodyCount(p_evidence_id). */
    public int getCustodyCount(int evidenceId) {
        Integer count = jdbc.queryForObject("SELECT GetCustodyCount(?)", Integer.class, evidenceId);
        return count != null ? count : 0;
    }

    /** Calls MySQL stored function GetAuditSummary(p_evidence_id). */
    public String getAuditSummary(int evidenceId) {
        return jdbc.queryForObject("SELECT GetAuditSummary(?)", String.class, evidenceId);
    }

    /** Upserts 1:1 Storage location for an evidence item (enforces UNIQUE(evidence_id)). */
    public void upsertStorageLocation(int evidenceId, String location) {
        jdbc.update(
                "INSERT INTO Storage (evidence_id, location) VALUES (?, ?) ON DUPLICATE KEY UPDATE location = VALUES(location)",
                evidenceId, location
        );
    }
}
