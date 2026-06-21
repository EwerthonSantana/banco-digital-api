package com.compass.bank.common.exception;

import java.math.BigDecimal;
import java.util.UUID;

/** Lancada quando o saldo da conta de origem e insuficiente. Mapeada para HTTP 422. */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID accountId, BigDecimal balance, BigDecimal amount) {
        super("Saldo insuficiente na conta " + accountId
                + ": saldo atual " + balance + ", valor solicitado " + amount);
    }
}
