package com.compass.bank.transfer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

/** Dados de uma solicitacao de transferencia. */
public record TransferRequest(

        @Schema(description = "Conta de origem (sera debitada)")
        @NotNull(message = "A conta de origem e obrigatoria")
        UUID sourceAccountId,

        @Schema(description = "Conta de destino (sera creditada)")
        @NotNull(message = "A conta de destino e obrigatoria")
        UUID destinationAccountId,

        @Schema(description = "Valor a transferir (deve ser positivo)", example = "150.00")
        @NotNull(message = "O valor e obrigatorio")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no maximo 2 casas decimais")
        BigDecimal amount
) {
}
