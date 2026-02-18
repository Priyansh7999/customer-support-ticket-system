package com.technogise.customerSupportTicketSystem.exception;

import com.technogise.customerSupportTicketSystem.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).
                body(new ErrorResponse(exception.getCode(), exception.getMessage()));
    }
    @ExceptionHandler(InvalidAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAssignmentException(InvalidAssignmentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                body(new ErrorResponse(exception.getCode(), exception.getMessage()));
    }
    @ExceptionHandler(ClosedTicketStatusException.class)
    public ResponseEntity<ErrorResponse> handleClosedTicketStatusException(ClosedTicketStatusException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).
                body(new ErrorResponse(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_DATA_FIELD", errorMessage));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        String errorMessage = "Missing required request header: " + ex.getHeaderName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("MISSING_USER_ID", errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST_BODY", "Required request body is missing"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid value for parameter '" + ex.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_PARAMETER_VALUE", errorMessage));
    }

    @ExceptionHandler(InvalidUserRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserRoleException(InvalidUserRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getCode(),ex.getMessage()));
    }
}
