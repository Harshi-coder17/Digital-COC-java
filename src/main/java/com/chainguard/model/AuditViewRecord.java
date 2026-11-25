package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;

public record AuditViewRecord(
    @JsonProperty("evidence_id") Integer evidenceId,
    @JsonProperty("evidence_type") String evidenceType,
    @JsonProperty("event_type") String eventType,
    @JsonProperty("actor") String actor,
    @JsonProperty("event_text") String eventText,
    @JsonProperty("event_time") Timestamp eventTime
) {}
