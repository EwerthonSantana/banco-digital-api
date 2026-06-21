package com.compass.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para atualizacao cadastral de uma conta.
 *
 * <p>Apenas dados cadastrais (nome) sao alteraveis por aqui. O saldo so muda
 * por transferencias, evitando alteracao manual indevida de dinheiro.</p>
 */
public record UpdateAccountRequest(

        @Schema(description = "Novo nome do titular", example = "Maria Silva Souza")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
        String name
) {
}
