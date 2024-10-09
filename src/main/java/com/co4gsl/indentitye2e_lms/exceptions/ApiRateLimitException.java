package com.co4gsl.indentitye2e_lms.exceptions;

import org.springframework.http.HttpStatus;

public class ApiRateLimitException extends BaseException {
    public ApiRateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}