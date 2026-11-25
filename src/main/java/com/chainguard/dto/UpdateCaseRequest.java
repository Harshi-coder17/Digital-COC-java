package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record UpdateCaseRequest(
    @JsonProperty("case_title") @Size(max = 200, message = "case_title must not exceed 200 characters.") String caseTitle,
    @JsonProperty("description") String description,
    @JsonProperty("status") String status
) {}
