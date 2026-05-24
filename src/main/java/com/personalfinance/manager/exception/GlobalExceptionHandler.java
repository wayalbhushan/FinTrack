package com.personalfinance.manager.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.lang.NonNull;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final @NonNull URI ABOUT_BLANK_URI = Objects.requireNonNull(URI.create("about:blank"));

    /**
     * Handles Jakarta Validation errors (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(ABOUT_BLANK_URI);

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    /**
     * Handles failed logins / bad credentials (401 Unauthorized).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles authorization check failures (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access is denied"
        );
        problemDetail.setTitle("Forbidden");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles duplicate registration usernames (409 Conflict).
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflict");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles general database integrity violations (409 Conflict).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Database integrity constraint violation occurred"
        );
        problemDetail.setTitle("Conflict");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles unmapped endpoints / resources (404 Not Found).
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Not Found");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles resource not found exceptions (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Not Found");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles resource conflict exceptions (409 Conflict).
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(ConflictException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflict");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Handles bad argument validation exceptions (400 Bad Request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }

    /**
     * Fallback for any unhandled general exception (500 Internal Server Error).
     * Strictly does not expose the internal error details or stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred on the server"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(ABOUT_BLANK_URI);
        return problemDetail;
    }
}
