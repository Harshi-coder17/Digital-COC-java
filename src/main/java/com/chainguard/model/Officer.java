package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Officer(
    @JsonProperty("officer_id") Integer officerId,
    @JsonProperty("name") String name,
    @JsonProperty("rank") String rank,
    @JsonProperty("department") String department,
    @JsonProperty("role") OfficerRole role,
    @JsonIgnore String passwordHash
) {}
