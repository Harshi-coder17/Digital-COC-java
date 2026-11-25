package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AppendFindingRequest(
    @JsonProperty("evidence_id") @NotNull(message = "evidence_id is required.") Integer evidenceId,
    @JsonProperty("finding_text") String findingText
) {}
