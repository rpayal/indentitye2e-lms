package com.co4gsl.indentitye2e_lms.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public abstract class BaseException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
    public BaseException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }

}
