package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEvidenceRequest(
    @JsonProperty("case_id") @NotNull(message = "case_id is required.") Integer caseId,
    @JsonProperty("type") @NotBlank(message = "type is required.") @Size(max = 100, message = "type must not exceed 100 characters.") String type,
    @JsonProperty("hash_value") @NotBlank(message = "hash_value is required.") @Size(max = 256, message = "hash_value must not exceed 256 characters.") String hashValue,
    @JsonProperty("status") String status,
    @JsonProperty("storage_location") String storageLocation
) {}
