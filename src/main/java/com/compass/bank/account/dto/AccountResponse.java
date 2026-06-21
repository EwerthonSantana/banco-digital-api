package com.compass.bank.account.dto;

import com.compass.bank.account.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Representacao de saida de uma conta. */
public record AccountResponse(

        @Schema(description = "Identificador unico da conta")
        UUID id,

        @Schema(description = "Nome do titular")
        String name,

        @Schema(description = "Saldo atual")
        BigDecimal balance,

        @Schema(description = "Data de criacao")
        Instant createdAt,

        @Schema(description = "Data da ultima atualizacao")
        Instant updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}
