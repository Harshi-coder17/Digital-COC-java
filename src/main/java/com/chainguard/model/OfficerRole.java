package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OfficerRole {
    ADMIN("admin"),
    OFFICER("officer"),
    ANALYST("analyst"),
    JUDICIAL("judicial");

    private final String dbValue;

    OfficerRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static OfficerRole fromValue(String value) {
        for (OfficerRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid officer role: " + value);
    }
}
