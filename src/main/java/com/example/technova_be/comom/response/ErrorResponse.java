package com.example.technova_be.comom.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String error;
    private String message;
    private Instant timestamp;

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(false, error, message, Instant.now());
    }
}
