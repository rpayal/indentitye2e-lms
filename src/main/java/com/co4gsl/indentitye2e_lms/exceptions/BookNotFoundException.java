package com.co4gsl.indentitye2e_lms.exceptions;

import org.springframework.http.HttpStatus;

public class BookNotFoundException extends BaseException {
    public BookNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}