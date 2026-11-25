package com.chainguard.repository;

import com.chainguard.model.CaseDetails;
import com.chainguard.model.CaseStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CaseRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<CaseDetails> ROW_MAPPER = (rs, rowNum) -> {
        Date sqlDate = rs.getDate("start_date");
        return new CaseDetails(
                rs.getInt("case_id"),
                rs.getString("case_title"),
                rs.getString("description"),
                sqlDate != null ? sqlDate.toLocalDate() : null,
                CaseStatus.fromValue(rs.getString("status"))
        );
    };

    public CaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CaseDetails> findAll() {
        return jdbc.query("SELECT * FROM Case_Details ORDER BY start_date DESC", ROW_MAPPER);
    }

    public Optional<CaseDetails> findById(int caseId) {
        List<CaseDetails> list = jdbc.query(
                "SELECT * FROM Case_Details WHERE case_id = ?",
                ROW_MAPPER,
                caseId
        );
        return list.stream().findFirst();
    }

    public int create(String caseTitle, String description, LocalDate startDate, CaseStatus status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Case_Details (case_title, description, start_date, status) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, caseTitle);
            ps.setString(2, description != null ? description : "");
            ps.setDate(3, Date.valueOf(startDate));
            ps.setString(4, status.getDbValue());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    public int update(int caseId, String caseTitle, String description, CaseStatus status) {
        return jdbc.update(
                "UPDATE Case_Details SET case_title = ?, description = ?, status = ? WHERE case_id = ?",
                caseTitle, description != null ? description : "", status.getDbValue(), caseId
        );
    }
}
