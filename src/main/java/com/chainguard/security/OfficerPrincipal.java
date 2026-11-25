package com.chainguard.security;

import com.chainguard.model.OfficerRole;

public record OfficerPrincipal(
    Integer officerId,
    OfficerRole role,
    String name
) {}
