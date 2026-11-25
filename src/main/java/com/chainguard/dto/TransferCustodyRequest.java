package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferCustodyRequest(
    @JsonProperty("evidence_id") @NotNull(message = "evidence_id is required.") Integer evidenceId,
    @JsonProperty("officer_id") @NotNull(message = "officer_id is required.") Integer officerId,
    @JsonProperty("action") @NotBlank(message = "action is required.") @Size(max = 200, message = "action must not exceed 200 characters.") String action,
    @JsonProperty("new_status") @NotBlank(message = "new_status is required.") String newStatus
) {}
