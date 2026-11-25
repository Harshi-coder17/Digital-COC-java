package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateForensicReportRequest(
    @JsonProperty("evidence_id") @NotNull(message = "evidence_id is required.") Integer evidenceId,
    @JsonProperty("report_date") LocalDate reportDate
) {}
