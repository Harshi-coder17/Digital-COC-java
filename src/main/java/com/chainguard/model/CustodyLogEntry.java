package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

public record CustodyLogEntry(
    @JsonProperty("log_id") Integer logId,
    @JsonProperty("evidence_id") Integer evidenceId,
    @JsonProperty("action") String action,
    @JsonProperty("action_time") Timestamp actionTime,
    @JsonProperty("officer_name") String officerName,
    @JsonProperty("officer_rank") String officerRank
) {}
