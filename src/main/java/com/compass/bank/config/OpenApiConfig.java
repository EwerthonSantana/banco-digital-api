package com.compass.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao da documentacao OpenAPI exibida no Swagger UI
 * (disponivel em /swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banco Digital API")
                        .description("API REST para gestao de contas, transferencia de fundos "
                                + "entre contas e consulta de movimentacoes financeiras. "
                                + "Projetada para garantir consistencia sob alta concorrencia.")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipe Banco Digital").email("dev@bancodigital.com"))
                        .license(new License().name("MIT")));
    }
}
