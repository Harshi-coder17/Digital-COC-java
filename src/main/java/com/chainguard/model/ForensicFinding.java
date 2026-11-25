package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

public record ForensicFinding(
    @JsonProperty("finding_id") Integer findingId,
    @JsonProperty("finding_text") String findingText,
    @JsonProperty("recorded_at") Timestamp recordedAt,
    @JsonProperty("reporter_name") String reporterName,
    @JsonProperty("reporter_rank") String reporterRank
) {}
