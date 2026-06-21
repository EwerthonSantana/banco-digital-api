package com.compass.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Ponto de entrada da aplicacao.
 *
 * <p>{@code @EnableAsync} habilita o processamento assincrono usado pelo
 * listener de notificacoes, garantindo que o envio da notificacao nao bloqueie
 * a thread que concluiu a transferencia.</p>
 */
@EnableAsync
@SpringBootApplication
public class BankApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApiApplication.class, args);
    }
}
