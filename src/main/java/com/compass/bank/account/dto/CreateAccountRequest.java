package com.compass.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Dados para criacao de uma nova conta. */
public record CreateAccountRequest(

        @Schema(description = "Nome do titular da conta", example = "Maria Silva")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String name,

        @Schema(description = "Saldo inicial da conta (>= 0)", example = "1000.00")
        @NotNull(message = "O saldo inicial e obrigatorio")
        @DecimalMin(value = "0.00", message = "O saldo inicial nao pode ser negativo")
        BigDecimal initialBalance
) {
}
