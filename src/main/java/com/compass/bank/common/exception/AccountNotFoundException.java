package com.compass.bank.common.exception;

import java.util.UUID;

/** Lancada quando uma conta referenciada nao existe. Mapeada para HTTP 404. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Conta nao encontrada: " + id);
    }
}
