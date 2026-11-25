package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Evidence(
    @JsonProperty("evidence_id") Integer evidenceId,
    @JsonProperty("case_id") Integer caseId,
    @JsonProperty("case_title") String caseTitle,
    @JsonProperty("type") String type,
    @JsonProperty("hash_value") String hashValue,
    @JsonProperty("status") EvidenceStatus status,
    @JsonProperty("storage_location") String storageLocation
) {}
