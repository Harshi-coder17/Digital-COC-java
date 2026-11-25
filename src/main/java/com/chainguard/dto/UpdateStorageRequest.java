package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStorageRequest(
    @JsonProperty("location") @NotBlank(message = "location is required.") @Size(max = 255, message = "location must not exceed 255 characters.") String location
) {}
