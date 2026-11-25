package com.chainguard.repository;

import com.chainguard.model.AuditViewRecord;
import com.chainguard.model.CourtViewRecord;
import com.chainguard.model.CustodyLogEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CustodyRepository {

    private final JdbcTemplate jdbc;

    public CustodyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CustodyLogEntry> findByEvidenceId(int evidenceId) {
        String sql = """
                SELECT cl.log_id, cl.evidence_id, cl.action, cl.action_time,
                       o.name AS officer_name, o.`rank` AS officer_rank
                FROM Custody_Log cl
                JOIN Officer o ON cl.officer_id = o.officer_id
                WHERE cl.evidence_id = ?
                ORDER BY cl.action_time ASC, cl.log_id ASC
                """;
        return jdbc.query(sql, (rs, rowNum) -> new CustodyLogEntry(
                rs.getInt("log_id"),
                rs.getInt("evidence_id"),
                rs.getString("action"),
                rs.getTimestamp("action_time"),
                rs.getString("officer_name"),
                rs.getString("officer_rank")
        ), evidenceId);
    }

    /**
     * Invokes MySQL stored procedure TransferCustody(p_evidence_id, p_officer_id, p_action, p_new_status).
     * Trigger trg_custody_force_timestamp enforces NOW() on action_time.
     */
    public void callTransferCustody(int evidenceId, int officerId, String action, String newStatus) {
        jdbc.update("CALL TransferCustody(?, ?, ?, ?)", evidenceId, officerId, action, newStatus);
    }

    /**
     * Attempted delete on Custody_Log — blocked by MySQL trigger trg_no_custody_delete (SQLState 45000).
     */
    public int deleteByLogId(int logId) {
        return jdbc.update("DELETE FROM Custody_Log WHERE log_id = ?", logId);
    }

    public List<CourtViewRecord> findCourtView() {
        return jdbc.query("SELECT * FROM Court_View", (rs, rowNum) -> new CourtViewRecord(
                rs.getInt("case_id"),
                rs.getString("case_title"),
                rs.getString("case_status"),
                rs.getInt("evidence_id"),
                rs.getString("evidence_type"),
                rs.getString("hash_value"),
                rs.getString("evidence_status"),
                rs.getString("handling_officer"),
                rs.getString("officer_rank"),
                rs.getString("custody_action"),
                rs.getTimestamp("action_time"),
                rs.getString("storage_location")
        ));
    }

    public List<AuditViewRecord> findAuditView() {
        return jdbc.query("SELECT * FROM Audit_View", (rs, rowNum) -> new AuditViewRecord(
                rs.getInt("evidence_id"),
                rs.getString("evidence_type"),
                rs.getString("event_type"),
                rs.getString("actor"),
                rs.getString("event_text"),
                rs.getTimestamp("event_time")
        ));
    }

    public List<Map<String, Object>> findCaseEvidenceCounts() {
        return jdbc.queryForList("SELECT * FROM Case_Evidence_Count");
    }

    public List<Map<String, Object>> findEvidenceCustodyStats() {
        return jdbc.queryForList("SELECT * FROM Evidence_Custody_Stats");
    }

    public List<Map<String, Object>> findEvidenceStatusDistribution() {
        return jdbc.queryForList("SELECT * FROM Evidence_Status_Distribution");
    }

    public List<Map<String, Object>> findCasesWithMultipleEvidence() {
        return jdbc.queryForList("SELECT * FROM Cases_With_Multiple_Evidence");
    }

    /** PDF Section 9.1.3 Subquery 1: Evidence items with NO forensic report filed yet. */
    public List<Map<String, Object>> findEvidenceWithoutReports() {
        return jdbc.queryForList("""
                SELECT evidence_id, type, status
                FROM Evidence
                WHERE evidence_id NOT IN (SELECT DISTINCT evidence_id FROM Forensic_Report)
                """);
    }

    /** PDF Section 9.1.3 Subquery 2: Officers who have NEVER handled any evidence. */
    public List<Map<String, Object>> findOfficersWithoutCustody() {
        return jdbc.queryForList("""
                SELECT officer_id, name, department
                FROM Officer
                WHERE officer_id NOT IN (SELECT DISTINCT officer_id FROM Custody_Log)
                """);
    }

    /** PDF Section 9.1.3 Subquery 3: Cases with NO evidence registered. */
    public List<Map<String, Object>> findCasesWithoutEvidence() {
        return jdbc.queryForList("""
                SELECT case_id, case_title
                FROM Case_Details
                WHERE case_id NOT IN (SELECT DISTINCT case_id FROM Evidence)
                """);
    }
}
