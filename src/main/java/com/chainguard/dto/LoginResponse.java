package com.chainguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("token") String token,
    @JsonProperty("role") String role,
    @JsonProperty("name") String name,
    @JsonProperty("officer_id") Integer officerId
) {}
