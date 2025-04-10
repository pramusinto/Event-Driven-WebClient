package com.scoring.model;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private int errorCode;
}
