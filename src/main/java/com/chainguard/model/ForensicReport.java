package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record ForensicReport(
    @JsonProperty("report_id") Integer reportId,
    @JsonProperty("evidence_id") Integer evidenceId,
    @JsonProperty("evidence_type") String evidenceType,
    @JsonProperty("analyst_name") String analystName,
    @JsonProperty("report_date") LocalDate reportDate
) {}
