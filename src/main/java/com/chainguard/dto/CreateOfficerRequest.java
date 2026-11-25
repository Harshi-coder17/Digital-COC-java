package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOfficerRequest(
    @JsonProperty("name") @NotBlank(message = "name is required.") @Size(max = 100, message = "name must not exceed 100 characters.") String name,
    @JsonProperty("rank") @NotBlank(message = "rank is required.") @Size(max = 50, message = "rank must not exceed 50 characters.") String rank,
    @JsonProperty("department") @NotBlank(message = "department is required.") @Size(max = 100, message = "department must not exceed 100 characters.") String department,
    @JsonProperty("role") String role,
    @JsonProperty("password") @NotBlank(message = "password is required.") @Size(min = 8, max = 128, message = "password must be 8-128 characters.") String password
) {}
