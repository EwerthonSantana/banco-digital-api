package com.compass.bank.common.exception;

/**
 * Lancada para transferencias logicamente invalidas (ex.: origem igual ao
 * destino). Mapeada para HTTP 400.
 */
public class InvalidTransferException extends RuntimeException {

    public InvalidTransferException(String message) {
        super(message);
    }
}
