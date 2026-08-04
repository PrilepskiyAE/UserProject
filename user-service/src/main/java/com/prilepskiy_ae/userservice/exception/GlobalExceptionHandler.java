package com.prilepskiy_ae.userservice.exception;

import com.prilepskiy_ae.userservice.dto.error.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException exception,
            ServletWebRequest request
    ) {
        String correlationId = getCorrelationId(request);
        log.warn("Correlation-ID={}, Пользователь не найден", correlationId, exception);

        ErrorResponse response = new ErrorResponse(
                "USER_NOT_FOUND",
                exception.getMessage(),
                LocalDateTime.now(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException exception,
            ServletWebRequest request
    ) {
        String correlationId = getCorrelationId(request);

        log.error("Correlation-ID={}, Необработанное исключение в API", correlationId, exception);

        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                "Произошла внутренняя ошибка сервиса. Попробуйте позже или сообщите оператору Correlation-ID: " + correlationId,
                LocalDateTime.now(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException exception,
            ServletWebRequest request
    ) {
        String correlationId = getCorrelationId(request);
        log.warn("Correlation-ID={}, Пользователь уже существует", correlationId, exception);

        ErrorResponse response = new ErrorResponse(
                "USER_ALREADY_EXISTS",
                exception.getMessage(),
                LocalDateTime.now(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            ServletWebRequest request
    ) {
        String correlationId = getCorrelationId(request);

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Ошибка валидации входных данных";
        }

        log.debug("Correlation-ID={}, Ошибка валидации запроса", correlationId, exception);

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                LocalDateTime.now(),
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            ServletWebRequest request) {
        String correlationId = getCorrelationId(request);
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                String.join("; ", errors),
                LocalDateTime.now(),
                correlationId
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    private String getCorrelationId(ServletWebRequest request) {
        return Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .orElse(UUID.randomUUID().toString());
    }
}
