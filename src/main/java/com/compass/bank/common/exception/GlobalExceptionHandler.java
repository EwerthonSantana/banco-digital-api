package com.compass.bank.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centraliza a traducao de excecoes em respostas HTTP padronizadas (ApiError),
 * mantendo os controllers limpos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalance(InsufficientBalanceException ex,
                                                              HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ApiError> handleInvalidTransfer(InvalidTransferException ex,
                                                          HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<ApiError.FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(),
                        "Falha de validacao nos dados enviados", request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        // Tipicamente uma corrida na chave de idempotencia (indice unico):
        // duas requisicoes identicas chegaram simultaneamente.
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(),
                        "Conflito ao persistir o recurso (possivel requisicao duplicada)",
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(),
                        "Erro inesperado: " + ex.getMessage(), request.getRequestURI()));
    }

    private ApiError.FieldValidationError toFieldError(FieldError error) {
        return new ApiError.FieldValidationError(error.getField(), error.getDefaultMessage());
    }
}
