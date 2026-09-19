package com.example.demo.shared.domain.exception;

public class InvalidSearchTokenException extends RuntimeException {

    public InvalidSearchTokenException(String message) {
        super(message);
    }
}

