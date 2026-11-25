package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateCaseRequest(
    @JsonProperty("case_title") @NotBlank(message = "case_title is required.") @Size(max = 200, message = "case_title must not exceed 200 characters.") String caseTitle,
    @JsonProperty("description") String description,
    @JsonProperty("start_date") @NotNull(message = "start_date is required.") LocalDate startDate,
    @JsonProperty("status") String status
) {}
