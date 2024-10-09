package com.co4gsl.indentitye2e_lms.exceptions.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private int status;
    private long timestamp;

    public ErrorResponse(String message, int status, long timestamp) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }
}
