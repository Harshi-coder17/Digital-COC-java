-- sql/routines_and_triggers.sql
-- Consolidated, idempotent Stored Procedures, Functions, Triggers, and Views
-- Run AFTER: sql/schema.sql

USE chain_of_custody;

-- ============================================================
-- 1. TRIGGERS
-- ============================================================
DELIMITER $$

DROP TRIGGER IF EXISTS trg_no_custody_delete$$
CREATE TRIGGER trg_no_custody_delete
    BEFORE DELETE ON Custody_Log
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'SECURITY VIOLATION: Custody_Log records are permanent and cannot be deleted.';
END$$

DROP TRIGGER IF EXISTS trg_no_findings_update$$
CREATE TRIGGER trg_no_findings_update
    BEFORE UPDATE ON Forensic_Findings_Log
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'SECURITY VIOLATION: Forensic findings are immutable. New findings must be appended only.';
END$$

DROP TRIGGER IF EXISTS trg_no_findings_delete$$
CREATE TRIGGER trg_no_findings_delete
    BEFORE DELETE ON Forensic_Findings_Log
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'SECURITY VIOLATION: Forensic findings cannot be deleted once recorded.';
END$$

DROP TRIGGER IF EXISTS trg_custody_force_timestamp$$
CREATE TRIGGER trg_custody_force_timestamp
    BEFORE INSERT ON Custody_Log
    FOR EACH ROW
BEGIN
    SET NEW.action_time = NOW();
END$$

DROP TRIGGER IF EXISTS trg_findings_force_timestamp$$
CREATE TRIGGER trg_findings_force_timestamp
    BEFORE INSERT ON Forensic_Findings_Log
    FOR EACH ROW
BEGIN
    SET NEW.recorded_at = NOW();
END$$

-- ============================================================
-- 2. STORED PROCEDURES & FUNCTIONS
-- ============================================================
DROP PROCEDURE IF EXISTS TransferCustody$$
CREATE PROCEDURE TransferCustody (
    IN p_evidence_id INT,
    IN p_officer_id INT,
    IN p_action VARCHAR(200),
    IN p_new_status VARCHAR(50)
)
BEGIN
    DECLARE v_ev_count INT DEFAULT 0;
    DECLARE v_off_count INT DEFAULT 0;

    IF p_action IS NULL OR LENGTH(TRIM(p_action)) = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Custody action description cannot be empty.';
    END IF;

    IF p_new_status NOT IN ('Collected', 'In Analysis', 'Stored', 'Presented') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Invalid evidence status.';
    END IF;

    SELECT COUNT(*) INTO v_ev_count
    FROM Evidence
    WHERE evidence_id = p_evidence_id;

    IF v_ev_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Evidence item not found.';
    END IF;

    SELECT COUNT(*) INTO v_off_count
    FROM Officer
    WHERE officer_id = p_officer_id;

    IF v_off_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Officer not found.';
    END IF;

    INSERT INTO Custody_Log (evidence_id, officer_id, action)
    VALUES (p_evidence_id, p_officer_id, p_action);

    UPDATE Evidence
    SET status = p_new_status
    WHERE evidence_id = p_evidence_id;
END$$

DROP PROCEDURE IF EXISTS AppendForensicFinding$$
CREATE PROCEDURE AppendForensicFinding (
    IN p_evidence_id INT,
    IN p_reported_by INT,
    IN p_finding_text TEXT
)
BEGIN
    DECLARE v_ev_count INT DEFAULT 0;
    DECLARE v_off_count INT DEFAULT 0;

    IF p_finding_text IS NULL OR LENGTH(TRIM(p_finding_text)) = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Finding text cannot be empty.';
    END IF;

    SELECT COUNT(*) INTO v_ev_count
    FROM Evidence
    WHERE evidence_id = p_evidence_id;

    IF v_ev_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Evidence item not found.';
    END IF;

    SELECT COUNT(*) INTO v_off_count
    FROM Officer
    WHERE officer_id = p_reported_by;

    IF v_off_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Reporting officer not found.';
    END IF;

    INSERT INTO Forensic_Findings_Log (evidence_id, reported_by, finding_text)
    VALUES (p_evidence_id, p_reported_by, p_finding_text);
END$$

DROP FUNCTION IF EXISTS GetCustodyCount$$
CREATE FUNCTION GetCustodyCount (p_evidence_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM Custody_Log
    WHERE evidence_id = p_evidence_id;

    RETURN v_count;
END$$

DROP FUNCTION IF EXISTS GetAuditSummary$$
CREATE FUNCTION GetAuditSummary (p_evidence_id INT)
RETURNS VARCHAR(500)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_custody_cnt INT DEFAULT 0;
    DECLARE v_findings_cnt INT DEFAULT 0;
    DECLARE v_reports_cnt INT DEFAULT 0;

    SELECT COUNT(*) INTO v_custody_cnt
    FROM Custody_Log
    WHERE evidence_id = p_evidence_id;

    SELECT COUNT(*) INTO v_findings_cnt
    FROM Forensic_Findings_Log
    WHERE evidence_id = p_evidence_id;

    SELECT COUNT(*) INTO v_reports_cnt
    FROM Forensic_Report
    WHERE evidence_id = p_evidence_id;

    RETURN CONCAT(
        'Evidence #', p_evidence_id,
        ' | Custody transfers: ', v_custody_cnt,
        ' | Forensic findings: ', v_findings_cnt,
        ' | Reports filed: ', v_reports_cnt
    );
END$$

DELIMITER ;

-- ============================================================
-- 3. VIEWS
-- ============================================================
CREATE OR REPLACE VIEW Court_View AS
SELECT
    cd.case_id,
    cd.case_title,
    cd.status           AS case_status,
    e.evidence_id,
    e.type              AS evidence_type,
    e.hash_value,
    e.status            AS evidence_status,
    o.name              AS handling_officer,
    o.rank              AS officer_rank,
    cl.action           AS custody_action,
    cl.action_time,
    s.location          AS storage_location
FROM   Case_Details  cd
JOIN   Evidence      e   ON cd.case_id     = e.case_id
JOIN   Custody_Log   cl  ON e.evidence_id  = cl.evidence_id
JOIN   Officer       o   ON cl.officer_id  = o.officer_id
LEFT JOIN Storage    s   ON e.evidence_id  = s.evidence_id
ORDER  BY cd.case_id, e.evidence_id, cl.action_time;

CREATE OR REPLACE VIEW Audit_View AS
SELECT
    e.evidence_id,
    e.type              AS evidence_type,
    'CUSTODY TRANSFER'  AS event_type,
    o.name              AS actor,
    cl.action           AS event_text,
    cl.action_time      AS event_time
FROM   Custody_Log cl
JOIN   Evidence    e  ON cl.evidence_id = e.evidence_id
JOIN   Officer     o  ON cl.officer_id  = o.officer_id

UNION ALL

SELECT
    e.evidence_id,
    e.type              AS evidence_type,
    'FORENSIC FINDING'  AS event_type,
    o.name              AS actor,
    ffl.finding_text    AS event_text,
    ffl.recorded_at     AS event_time
FROM   Forensic_Findings_Log ffl
JOIN   Evidence  e  ON ffl.evidence_id = e.evidence_id
JOIN   Officer   o  ON ffl.reported_by = o.officer_id

ORDER  BY evidence_id, event_time;

CREATE OR REPLACE VIEW Case_Evidence_Count AS
SELECT
    cd.case_id,
    cd.case_title,
    COUNT(e.evidence_id) AS evidence_count
FROM   Case_Details cd
LEFT JOIN Evidence e ON cd.case_id = e.case_id
GROUP  BY cd.case_id, cd.case_title;

CREATE OR REPLACE VIEW Evidence_Custody_Stats AS
SELECT
    evidence_id,
    COUNT(*)         AS transfer_count,
    MIN(action_time) AS first_custody,
    MAX(action_time) AS latest_custody
FROM   Custody_Log
GROUP  BY evidence_id;

CREATE OR REPLACE VIEW Evidence_Status_Distribution AS
SELECT
    status,
    COUNT(*) AS total_items
FROM Evidence
GROUP BY status;

CREATE OR REPLACE VIEW Cases_With_Multiple_Evidence AS
SELECT
    cd.case_id,
    cd.case_title,
    COUNT(e.evidence_id) AS evidence_count
FROM Case_Details cd
JOIN Evidence e ON cd.case_id = e.case_id
GROUP BY cd.case_id, cd.case_title
HAVING COUNT(e.evidence_id) > 1;
