package com.compass.bank.transfer.dto;

import com.compass.bank.transfer.Transfer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Uma movimentacao financeira do ponto de vista de uma conta especifica.
 * A mesma transferencia aparece como DEBIT para a origem e CREDIT para o destino.
 */
public record MovementResponse(

        @Schema(description = "Transferencia que originou a movimentacao")
        UUID transferId,

        @Schema(description = "Direcao em relacao a conta consultada", example = "DEBIT")
        Direction direction,

        @Schema(description = "Conta da contraparte")
        UUID counterpartyAccountId,

        @Schema(description = "Nome da contraparte")
        String counterpartyName,

        @Schema(description = "Valor da movimentacao")
        BigDecimal amount,

        @Schema(description = "Momento da movimentacao")
        Instant createdAt
) {

    public enum Direction {
        DEBIT,
        CREDIT
    }

    /** Projeta a transferencia sob a otica da conta informada. */
    public static MovementResponse forAccount(Transfer transfer, UUID accountId) {
        boolean isSource = transfer.getSourceAccount().getId().equals(accountId);
        if (isSource) {
            return new MovementResponse(
                    transfer.getId(),
                    Direction.DEBIT,
                    transfer.getDestinationAccount().getId(),
                    transfer.getDestinationAccount().getName(),
                    transfer.getAmount(),
                    transfer.getCreatedAt());
        }
        return new MovementResponse(
                transfer.getId(),
                Direction.CREDIT,
                transfer.getSourceAccount().getId(),
                transfer.getSourceAccount().getName(),
                transfer.getAmount(),
                transfer.getCreatedAt());
    }
}
