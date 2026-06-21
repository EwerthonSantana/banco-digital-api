package com.compass.bank.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Corpo padronizado de resposta de erro da API. */
public record ApiError(

        @Schema(description = "Momento em que o erro ocorreu")
        Instant timestamp,

        @Schema(description = "Codigo HTTP", example = "422")
        int status,

        @Schema(description = "Descricao do codigo HTTP", example = "Unprocessable Entity")
        String error,

        @Schema(description = "Mensagem legivel do erro")
        String message,

        @Schema(description = "Caminho da requisicao", example = "/api/v1/transfers")
        String path,

        @Schema(description = "Erros de validacao por campo (quando aplicavel)")
        List<FieldValidationError> fieldErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldValidationError> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }

    /** Detalhe de um erro de validacao de campo. */
    public record FieldValidationError(String field, String message) {
    }
}
