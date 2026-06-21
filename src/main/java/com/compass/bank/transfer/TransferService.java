package com.compass.bank.transfer;

import com.compass.bank.account.Account;
import com.compass.bank.account.AccountRepository;
import com.compass.bank.common.exception.AccountNotFoundException;
import com.compass.bank.common.exception.InvalidTransferException;
import com.compass.bank.notification.TransferCompletedEvent;
import com.compass.bank.transfer.dto.MovementResponse;
import com.compass.bank.transfer.dto.TransferRequest;
import com.compass.bank.transfer.dto.TransferResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Casos de uso de transferencia e consulta de movimentacoes.
 *
 * <p><b>Consistencia sob concorrencia:</b> as duas contas envolvidas sao
 * carregadas com lock pessimista de escrita ({@code SELECT ... FOR UPDATE}).
 * Para evitar deadlocks quando duas transferencias mexem nas mesmas contas em
 * sentidos opostos, os locks sao sempre adquiridos em ordem deterministica
 * (ordenando pelos UUIDs das contas).</p>
 *
 * <p><b>Idempotencia:</b> uma {@code Idempotency-Key} opcional evita que
 * retries de rede gerem transferencias duplicadas.</p>
 */
@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(AccountRepository accountRepository,
                           TransferRepository transferRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String idempotencyKey) {
        validate(request);

        String key = StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null;
        if (key != null) {
            Optional<Transfer> previous = transferRepository.findByIdempotencyKey(key);
            if (previous.isPresent()) {
                // Requisicao repetida: devolve o resultado original sem reprocessar.
                return TransferResponse.from(previous.get());
            }
        }

        // Locks adquiridos em ordem deterministica para evitar deadlock.
        UUID firstId = min(request.sourceAccountId(), request.destinationAccountId());
        UUID secondId = max(request.sourceAccountId(), request.destinationAccountId());
        Account first = lockAccount(firstId);
        Account second = lockAccount(secondId);

        Account source;
        Account destination;
        if (firstId.equals(request.sourceAccountId())) {
            source = first;
            destination = second;
        } else {
            source = second;
            destination = first;
        }

        // Regras de saldo ficam na entidade; debit() lanca se faltar saldo.
        source.debit(request.amount());
        destination.credit(request.amount());

        Transfer transfer = transferRepository.save(
                new Transfer(source, destination, request.amount(), key));

        // O evento so vira notificacao APOS o commit (ver NotificationService).
        eventPublisher.publishEvent(new TransferCompletedEvent(
                transfer.getId(),
                source.getId(), source.getName(),
                destination.getId(), destination.getName(),
                transfer.getAmount(),
                Instant.now()));

        return TransferResponse.from(transfer);
    }

    @Transactional(readOnly = true)
    public Page<MovementResponse> getMovements(UUID accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return transferRepository.findMovements(accountId, pageable)
                .map(transfer -> MovementResponse.forAccount(transfer, accountId));
    }

    private void validate(TransferRequest request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new InvalidTransferException("A conta de origem e destino nao podem ser iguais");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new InvalidTransferException("O valor da transferencia deve ser positivo");
        }
    }

    private Account lockAccount(UUID id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    private static UUID min(UUID a, UUID b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static UUID max(UUID a, UUID b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}
