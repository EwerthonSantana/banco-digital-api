package com.compass.bank.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.compass.bank.account.dto.AccountResponse;
import com.compass.bank.account.dto.CreateAccountRequest;
import com.compass.bank.account.dto.UpdateAccountRequest;
import com.compass.bank.common.exception.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Cria conta persistindo nome e saldo inicial")
    void create() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.create(
                new CreateAccountRequest("Maria Silva", new BigDecimal("1000.00")));

        assertThat(response.name()).isEqualTo("Maria Silva");
        assertThat(response.balance()).isEqualByComparingTo("1000.00");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Busca conta existente por ID")
    void getByIdFound() {
        UUID id = UUID.randomUUID();
        Account account = new Account("Joao", new BigDecimal("50.00"));
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getById(id);

        assertThat(response.name()).isEqualTo("Joao");
    }

    @Test
    @DisplayName("Lanca excecao ao buscar conta inexistente")
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(id))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Atualiza o nome da conta")
    void update() {
        UUID id = UUID.randomUUID();
        Account account = new Account("Nome Antigo", new BigDecimal("10.00"));
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.update(id, new UpdateAccountRequest("Nome Novo"));

        assertThat(response.name()).isEqualTo("Nome Novo");
        assertThat(account.getName()).isEqualTo("Nome Novo");
    }

    @Test
    @DisplayName("Remove conta existente")
    void delete() {
        UUID id = UUID.randomUUID();
        Account account = new Account("Ana", new BigDecimal("10.00"));
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        accountService.delete(id);

        verify(accountRepository).delete(account);
    }
}
