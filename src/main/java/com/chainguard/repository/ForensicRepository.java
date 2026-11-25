package com.chainguard.repository;

import com.chainguard.model.ForensicFinding;
import com.chainguard.model.ForensicReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ForensicRepository {

    private final JdbcTemplate jdbc;

    public ForensicRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ForensicFinding> findByEvidenceId(int evidenceId) {
        String sql = """
                SELECT ffl.finding_id, ffl.finding_text, ffl.recorded_at,
                       o.name AS reporter_name, o.`rank` AS reporter_rank
                FROM Forensic_Findings_Log ffl
                JOIN Officer o ON ffl.reported_by = o.officer_id
                WHERE ffl.evidence_id = ?
                ORDER BY ffl.recorded_at ASC, ffl.finding_id ASC
                """;
        return jdbc.query(sql, (rs, rowNum) -> new ForensicFinding(
                rs.getInt("finding_id"),
                rs.getString("finding_text"),
                rs.getTimestamp("recorded_at"),
                rs.getString("reporter_name"),
                rs.getString("reporter_rank")
        ), evidenceId);
    }

    /**
     * Invokes MySQL stored procedure AppendForensicFinding(p_evidence_id, p_reported_by, p_finding_text).
     * Trigger trg_findings_force_timestamp enforces NOW() on recorded_at.
     */
    public void callAppendForensicFinding(int evidenceId, int reportedBy, String findingText) {
        jdbc.update("CALL AppendForensicFinding(?, ?, ?)", evidenceId, reportedBy, findingText);
    }

    /**
     * Attempted update on Forensic_Findings_Log — blocked by MySQL trigger trg_no_findings_update (SQLState 45000).
     */
    public int updateFinding(int findingId, String findingText) {
        return jdbc.update("UPDATE Forensic_Findings_Log SET finding_text = ? WHERE finding_id = ?", findingText, findingId);
    }

    /**
     * Attempted delete on Forensic_Findings_Log — blocked by MySQL trigger trg_no_findings_delete (SQLState 45000).
     */
    public int deleteFinding(int findingId) {
        return jdbc.update("DELETE FROM Forensic_Findings_Log WHERE finding_id = ?", findingId);
    }

    public List<ForensicReport> findAllReports(Integer evidenceId) {
        String base = """
                SELECT fr.report_id, fr.evidence_id, e.type AS evidence_type, fr.analyst_name, fr.report_date
                FROM Forensic_Report fr
                JOIN Evidence e ON fr.evidence_id = e.evidence_id
                """;
        if (evidenceId != null) {
            return jdbc.query(base + " WHERE fr.evidence_id = ? ORDER BY fr.report_date DESC, fr.report_id DESC",
                    (rs, rowNum) -> new ForensicReport(
                            rs.getInt("report_id"),
                            rs.getInt("evidence_id"),
                            rs.getString("evidence_type"),
                            rs.getString("analyst_name"),
                            rs.getDate("report_date").toLocalDate()
                    ), evidenceId);
        }
        return jdbc.query(base + " ORDER BY fr.report_date DESC, fr.report_id DESC",
                (rs, rowNum) -> new ForensicReport(
                        rs.getInt("report_id"),
                        rs.getInt("evidence_id"),
                        rs.getString("evidence_type"),
                        rs.getString("analyst_name"),
                        rs.getDate("report_date").toLocalDate()
                ));
    }

    public int createReport(int evidenceId, String analystName, LocalDate reportDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Forensic_Report (evidence_id, analyst_name, report_date) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, evidenceId);
            ps.setString(2, analystName);
            ps.setDate(3, Date.valueOf(reportDate));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }
}
