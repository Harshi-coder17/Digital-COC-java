-- sql/schema.sql
-- Digital Evidence Chain-of-Custody Database (ChainGuard)
-- Engine: MySQL 8.0+ (InnoDB, 3NF)
-- Idempotent: safe to re-run in MySQL Workbench or mysql CLI

CREATE DATABASE IF NOT EXISTS chain_of_custody
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE chain_of_custody;

-- ─────────────────────────────────────────────────
-- TABLE 1: Case_Details
-- Stores all registered investigation cases.
-- Canonical status ENUM: 'Open', 'Closed', 'Under Investigation'
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Case_Details (
    case_id     INT           NOT NULL AUTO_INCREMENT,
    case_title  VARCHAR(200)  NOT NULL,
    description TEXT,
    start_date  DATE          NOT NULL,
    status      ENUM(
                  'Open',
                  'Closed',
                  'Under Investigation'
                ) NOT NULL DEFAULT 'Open',
    PRIMARY KEY (case_id)
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 2: Officer
-- Authorized personnel. Includes BCrypt login credentials.
-- Canonical role ENUM: 'admin', 'officer', 'analyst', 'judicial'
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Officer (
    officer_id    INT           NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL,
    `rank`        VARCHAR(50)   NOT NULL,
    department    VARCHAR(100)  NOT NULL,
    role          ENUM(
                    'admin',
                    'officer',
                    'analyst',
                    'judicial'
                  ) NOT NULL DEFAULT 'officer',
    password_hash VARCHAR(255)  NOT NULL,
    PRIMARY KEY (officer_id)
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 3: Evidence
-- Digital evidence items linked to a case.
-- hash_value is NOT NULL — legal integrity check.
-- Canonical status ENUM: 'Collected', 'In Analysis', 'Stored', 'Presented'
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Evidence (
    evidence_id  INT           NOT NULL AUTO_INCREMENT,
    case_id      INT           NOT NULL,
    type         VARCHAR(100)  NOT NULL,
    hash_value   VARCHAR(256)  NOT NULL,
    status       ENUM(
                   'Collected',
                   'In Analysis',
                   'Stored',
                   'Presented'
                 ) NOT NULL DEFAULT 'Collected',
    PRIMARY KEY (evidence_id),
    INDEX idx_evidence_case_id (case_id),
    CONSTRAINT fk_evidence_case
        FOREIGN KEY (case_id)
        REFERENCES Case_Details (case_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 4: Custody_Log
-- Full chain-of-custody history per evidence item.
-- DELETE is blocked by trigger trg_no_custody_delete.
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Custody_Log (
    log_id       INT           NOT NULL AUTO_INCREMENT,
    evidence_id  INT           NOT NULL,
    officer_id   INT           NOT NULL,
    action       VARCHAR(200)  NOT NULL,
    action_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    INDEX idx_custody_evidence_id (evidence_id),
    INDEX idx_custody_officer_id (officer_id),
    CONSTRAINT fk_custody_evidence
        FOREIGN KEY (evidence_id)
        REFERENCES Evidence (evidence_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_custody_officer
        FOREIGN KEY (officer_id)
        REFERENCES Officer (officer_id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 5: Forensic_Report
-- Official forensic analysis summary per evidence.
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Forensic_Report (
    report_id    INT           NOT NULL AUTO_INCREMENT,
    evidence_id  INT           NOT NULL,
    analyst_name VARCHAR(100)  NOT NULL,
    report_date  DATE          NOT NULL,
    PRIMARY KEY (report_id),
    CONSTRAINT fk_report_evidence
        FOREIGN KEY (evidence_id)
        REFERENCES Evidence (evidence_id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 6: Forensic_Findings_Log  [APPEND-ONLY]
-- Detailed findings. UPDATE + DELETE blocked by triggers.
-- finding_text is NOT NULL — empty findings not allowed.
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Forensic_Findings_Log (
    finding_id   INT       NOT NULL AUTO_INCREMENT,
    evidence_id  INT       NOT NULL,
    reported_by  INT       NOT NULL,
    finding_text TEXT      NOT NULL,
    recorded_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (finding_id),
    INDEX idx_findings_evidence_id (evidence_id),
    CONSTRAINT fk_finding_evidence
        FOREIGN KEY (evidence_id)
        REFERENCES Evidence (evidence_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_finding_officer
        FOREIGN KEY (reported_by)
        REFERENCES Officer (officer_id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ─────────────────────────────────────────────────
-- TABLE 7: Storage
-- Physical location of each evidence item.
-- UNIQUE on evidence_id enforces 1:1 relationship.
-- ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Storage (
    storage_id   INT           NOT NULL AUTO_INCREMENT,
    evidence_id  INT           NOT NULL,
    location     VARCHAR(255)  NOT NULL,
    PRIMARY KEY (storage_id),
    UNIQUE KEY uq_storage_evidence (evidence_id),
    CONSTRAINT fk_storage_evidence
        FOREIGN KEY (evidence_id)
        REFERENCES Evidence (evidence_id)
        ON DELETE RESTRICT
) ENGINE = InnoDB;
