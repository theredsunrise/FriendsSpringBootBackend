package com.example.demo.shared.infrastructure.in.controller.exception;

import com.example.demo.friendship.application.exception.FriendshipException;
import com.example.demo.shared.domain.exception.InvalidSearchTokenException;
import com.example.demo.user.application.exception.UserException;
import com.example.demo.user.application.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.*;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FriendshipException.class)
    public ProblemDetail handleFriendshipException(final FriendshipException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        problemDetail.setTitle("Friendship Error");
        problemDetail.setType(URI.create("example.com"));

        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(final UserNotFoundException ex) {
        log.warn(ex.getMessage(), ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        problemDetail.setTitle("User Error");
        problemDetail.setType(URI.create("example.com"));

        return problemDetail;
    }

    @ExceptionHandler(UserException.class)
    public ProblemDetail handleUserException(final UserException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        problemDetail.setTitle("User Error");
        problemDetail.setType(URI.create("example.com"));

        return problemDetail;
    }

    @ExceptionHandler(InvalidSearchTokenException.class)
    public ProblemDetail handleInvalidSearchToken(final InvalidSearchTokenException ex) {
        log.warn(ex.getMessage());
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                );

        problemDetail.setTitle("Token Error");
        problemDetail.setType(URI.create("example.com"));

        return problemDetail;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn(ex.getMessage());
        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodValidationException(MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn(ex.getMessage());
        return super.handleMethodValidationException(ex, headers, status, request);
    }

    //    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ProblemDetail handleValidationErrors(final MethodArgumentNotValidException ex) {
//
//        log.error("Method argument exception occurred: ", ex);
//
//        ProblemDetail problemDetail =
//                ProblemDetail.forStatusAndDetail(
//                        HttpStatus.BAD_REQUEST,
//                        "Validation failed for request body."
//                );
//
//        problemDetail.setTitle("Validation Error");
//        problemDetail.setType(URI.create("example.com"));
//
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult()
//                .getFieldErrors()
//                .forEach(error ->
//                        errors.put(
//                                error.getField(),
//                                error.getDefaultMessage()
//                        )
//                );
//
//        problemDetail.setProperty(
//                "invalid_fields",
//                errors
//        );
//
//        return problemDetail;
//    }
//
//    @ExceptionHandler(HttpMessageNotReadableException.class)
//    public ProblemDetail handleHttpMessageNotReadable(final HttpMessageNotReadableException ex) {
//        log.error("Malformed JSON request received: {}", ex.getMessage());
//
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
//                HttpStatus.BAD_REQUEST,
//                "Malformed JSON request body or invalid data format."
//        );
//
//        problemDetail.setTitle("Malformed JSON");
//        problemDetail.setType(URI.create("example.com"));
//        problemDetail.setProperty("error_reason", ex.getMostSpecificCause().getMessage());
//
//        return problemDetail;
//    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(final Exception ex) {
        log.error(ex.getMessage(), ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An internal server error occurred. Please contact system support."
                );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("example.com"));

        return problemDetail;
    }
}

