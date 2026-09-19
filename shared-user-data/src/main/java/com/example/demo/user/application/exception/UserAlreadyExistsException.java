package com.example.demo.user.application.exception;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

