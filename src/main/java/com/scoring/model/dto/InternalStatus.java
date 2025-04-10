package com.scoring.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
public enum InternalStatus {
    DEFAULT("Default"), PROCESSED("Processed Scoring"),
    TIMEOUT("Timeout When Call Scoring"), INTERNAL_SERVER_ERROR("Internal Server Error"),
    BAD_REQUEST("Bad Request");

    private final String label;
    InternalStatus(String label) {
        this.label = label;
    }
}
