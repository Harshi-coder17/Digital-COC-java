package com.chainguard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CaseStatus {
    OPEN("Open"),
    CLOSED("Closed"),
    UNDER_INVESTIGATION("Under Investigation");

    private final String dbValue;

    CaseStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static CaseStatus fromValue(String value) {
        for (CaseStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid case status: " + value);
    }
}
