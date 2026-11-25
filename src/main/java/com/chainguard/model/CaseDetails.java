package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record CaseDetails(
    @JsonProperty("case_id") Integer caseId,
    @JsonProperty("case_title") String caseTitle,
    @JsonProperty("description") String description,
    @JsonProperty("start_date") LocalDate startDate,
    @JsonProperty("status") CaseStatus status
) {}
