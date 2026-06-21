package com.compass.bank.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.compass.bank.account.dto.AccountResponse;
import com.compass.bank.account.dto.CreateAccountRequest;
import com.compass.bank.common.exception.AccountNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("POST /accounts cria conta e retorna 201")
    void createReturns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.create(any(CreateAccountRequest.class)))
                .thenReturn(new AccountResponse(id, "Maria Silva", new BigDecimal("1000.00"),
                        Instant.now(), Instant.now()));

        CreateAccountRequest request = new CreateAccountRequest("Maria Silva", new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Maria Silva"));
    }

    @Test
    @DisplayName("POST /accounts com nome em branco retorna 400 e nao chama o servico")
    void createValidationError() throws Exception {
        CreateAccountRequest invalid = new CreateAccountRequest("", new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(accountService, never()).create(any());
    }

    @Test
    @DisplayName("GET /accounts/{id} inexistente retorna 404 padronizado")
    void getByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.getById(id)).thenThrow(new AccountNotFoundException(id));

        mockMvc.perform(get("/api/v1/accounts/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
