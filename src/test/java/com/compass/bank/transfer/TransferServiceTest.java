package com.compass.bank.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.compass.bank.account.Account;
import com.compass.bank.account.AccountRepository;
import com.compass.bank.common.exception.AccountNotFoundException;
import com.compass.bank.common.exception.InsufficientBalanceException;
import com.compass.bank.common.exception.InvalidTransferException;
import com.compass.bank.notification.TransferCompletedEvent;
import com.compass.bank.transfer.dto.TransferRequest;
import com.compass.bank.transfer.dto.TransferResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TransferService transferService;

    private UUID sourceId;
    private UUID destId;
    private Account source;
    private Account dest;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountRepository, transferRepository, eventPublisher);
        sourceId = UUID.randomUUID();
        destId = UUID.randomUUID();
        source = account(sourceId, "Maria", "100.00");
        dest = account(destId, "Joao", "50.00");
    }

    @Test
    @DisplayName("Transferencia valida debita origem, credita destino e publica evento")
    void transferSuccess() {
        when(accountRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(destId)).thenReturn(Optional.of(dest));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> {
            Transfer t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", UUID.randomUUID());
            return t;
        });

        TransferResponse response = transferService.transfer(
                new TransferRequest(sourceId, destId, new BigDecimal("30.00")), null);

        assertThat(source.getBalance()).isEqualByComparingTo("70.00");
        assertThat(dest.getBalance()).isEqualByComparingTo("80.00");
        assertThat(response.amount()).isEqualByComparingTo("30.00");
        assertThat(response.sourceBalanceAfter()).isEqualByComparingTo("70.00");
        verify(transferRepository).save(any(Transfer.class));
        verify(eventPublisher).publishEvent(any(TransferCompletedEvent.class));
    }

    @Test
    @DisplayName("Saldo insuficiente impede a transferencia")
    void transferInsufficientBalance() {
        when(accountRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(destId)).thenReturn(Optional.of(dest));

        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(sourceId, destId, new BigDecimal("999.00")), null))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(source.getBalance()).isEqualByComparingTo("100.00");
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Origem igual ao destino e rejeitada")
    void transferSameAccount() {
        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(sourceId, sourceId, new BigDecimal("10.00")), null))
                .isInstanceOf(InvalidTransferException.class);

        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    @DisplayName("Conta inexistente gera erro de conta nao encontrada")
    void transferAccountNotFound() {
        when(accountRepository.findByIdForUpdate(ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(sourceId, destId, new BigDecimal("10.00")), null))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("Idempotency-Key repetida retorna a transferencia original sem reprocessar")
    void transferIdempotent() {
        Transfer existing = new Transfer(source, dest, new BigDecimal("30.00"), "key-123");
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());
        when(transferRepository.findByIdempotencyKey("key-123")).thenReturn(Optional.of(existing));

        TransferResponse response = transferService.transfer(
                new TransferRequest(sourceId, destId, new BigDecimal("30.00")), "key-123");

        assertThat(response.amount()).isEqualByComparingTo("30.00");
        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(transferRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private static Account account(UUID id, String name, String balance) {
        Account account = new Account(name, new BigDecimal(balance));
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}
