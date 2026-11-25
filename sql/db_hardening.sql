-- sql/db_hardening.sql
-- Least-Privilege MySQL Application User for ChainGuard
-- Run as root AFTER schema.sql and routines_and_triggers.sql.
-- Replace 'REPLACE_WITH_STRONG_MYSQL_PASSWORD' before executing!

CREATE USER IF NOT EXISTS 'chainguard_app'@'127.0.0.1'
    IDENTIFIED BY 'REPLACE_WITH_STRONG_MYSQL_PASSWORD';

-- Grant only required DML + stored routine execution (NO DROP, ALTER, CREATE, SUPER, FILE, GRANT)
GRANT SELECT, INSERT, UPDATE, EXECUTE
    ON chain_of_custody.*
    TO 'chainguard_app'@'127.0.0.1';

-- Revoke DELETE on immutable audit tables at the privilege level as well (triggers also enforce this)
REVOKE DELETE ON chain_of_custody.Custody_Log FROM 'chainguard_app'@'127.0.0.1';
REVOKE DELETE, UPDATE ON chain_of_custody.Forensic_Findings_Log FROM 'chainguard_app'@'127.0.0.1';

FLUSH PRIVILEGES;
