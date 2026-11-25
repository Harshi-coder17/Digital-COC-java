package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EvidenceStatus {
    COLLECTED("Collected"),
    IN_ANALYSIS("In Analysis"),
    STORED("Stored"),
    PRESENTED("Presented");

    private final String dbValue;

    EvidenceStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static EvidenceStatus fromValue(String value) {
        for (EvidenceStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid evidence status: " + value);
    }
}
