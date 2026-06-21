package com.compass.bank.transfer.dto;

import com.compass.bank.transfer.Transfer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Resultado de uma transferencia concluida. */
public record TransferResponse(

        @Schema(description = "Identificador da transferencia")
        UUID transferId,

        @Schema(description = "Conta de origem")
        UUID sourceAccountId,

        @Schema(description = "Conta de destino")
        UUID destinationAccountId,

        @Schema(description = "Valor transferido")
        BigDecimal amount,

        @Schema(description = "Saldo da conta de origem apos a transferencia")
        BigDecimal sourceBalanceAfter,

        @Schema(description = "Momento da transferencia")
        Instant createdAt
) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                transfer.getSourceAccount().getBalance(),
                transfer.getCreatedAt());
    }
}
