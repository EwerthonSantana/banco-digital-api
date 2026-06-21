package com.compass.bank.notification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado quando uma transferencia e concluida com sucesso.
 * Desacopla a regra de negocio (transferir) do efeito colateral (notificar).
 */
public record TransferCompletedEvent(
        UUID transferId,
        UUID sourceAccountId,
        String sourceAccountName,
        UUID destinationAccountId,
        String destinationAccountName,
        BigDecimal amount,
        Instant occurredAt) {
}
