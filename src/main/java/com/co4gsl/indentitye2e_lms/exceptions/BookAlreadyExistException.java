package com.co4gsl.indentitye2e_lms.exceptions;

import org.springframework.http.HttpStatus;

public class BookAlreadyExistException extends BaseException {
    public BookAlreadyExistException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}