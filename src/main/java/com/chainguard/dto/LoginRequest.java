package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @JsonProperty("officer_id") @NotNull(message = "officer_id is required.") Integer officerId,
    @JsonProperty("password") @NotBlank(message = "password is required.") String password
) {}
