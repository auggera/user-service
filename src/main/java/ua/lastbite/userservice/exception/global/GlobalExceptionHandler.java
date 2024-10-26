package ua.lastbite.userservice.exception.global;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import ua.lastbite.userservice.exception.token.*;
import ua.lastbite.userservice.exception.user.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<String> handleTokenValidationException(TokenValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<String> handlePhoneNumberAlreadyExists(PhoneNumberAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IncorrectCurrentPasswordException.class)
    public ResponseEntity<String> handleIncorrectCurrentPassword(IncorrectCurrentPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EmailAddressNotChangedException.class)
    public ResponseEntity<String> handleEmailNotChanged(EmailAddressNotChangedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PasswordNotChangedException.class)
    public ResponseEntity<String> handleSamePassword(PasswordNotChangedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PhoneNumberNotChangedException.class)
    public ResponseEntity<String> handleSamePhoneNumber(PhoneNumberNotChangedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String argumentName = ex.getName();
        Object value = ex.getValue();
        Class<?> expectedType = ex.getRequiredType();

        if ("role".equals(argumentName) && expectedType != null && expectedType.isEnum()) {
            String message = String.format("Invalid value for role: %s. Allowed values are: CUSTOMER, BUSINESS_OWNER, ADMIN", value);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                String.format("Invalid value for %s: %s", argumentName, value)
        );
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleTokenNotFound(TokenNotFoundException ex) {
        LOGGER.error("Handled TokenNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<String> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {
        LOGGER.error("Handled TokenAlreadyUsedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handleTokenExpiredException(TokenExpiredException ex) {
        LOGGER.error("Handled TokenExpiredException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<String> handleServiceUnavailable(ServiceUnavailableException ex) {
        LOGGER.error("Handled ServiceUnavailableException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    @ExceptionHandler(NameNotChangedException.class)
    public ResponseEntity<String> handleNameNotChanged(NameNotChangedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}