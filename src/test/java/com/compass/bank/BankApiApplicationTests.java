package com.compass.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Garante que o contexto da aplicacao sobe corretamente (smoke test). */
@SpringBootTest
class BankApiApplicationTests {

    @Test
    @DisplayName("O contexto Spring carrega sem erros")
    void contextLoads() {
        // se o contexto falhar ao subir, o teste falha
    }
}
