package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

public record CourtViewRecord(
    @JsonProperty("case_id") Integer caseId,
    @JsonProperty("case_title") String caseTitle,
    @JsonProperty("case_status") String caseStatus,
    @JsonProperty("evidence_id") Integer evidenceId,
    @JsonProperty("evidence_type") String evidenceType,
    @JsonProperty("hash_value") String hashValue,
    @JsonProperty("evidence_status") String evidenceStatus,
    @JsonProperty("handling_officer") String handlingOfficer,
    @JsonProperty("officer_rank") String officerRank,
    @JsonProperty("custody_action") String custodyAction,
    @JsonProperty("action_time") Timestamp actionTime,
    @JsonProperty("storage_location") String storageLocation
) {}
