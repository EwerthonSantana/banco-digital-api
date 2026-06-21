package com.compass.bank.account;

import com.compass.bank.account.dto.AccountResponse;
import com.compass.bank.account.dto.CreateAccountRequest;
import com.compass.bank.account.dto.UpdateAccountRequest;
import com.compass.bank.common.exception.AccountNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de gestao de contas (CRUD). */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        Account account = new Account(request.name(), request.initialBalance());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        return AccountResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> list(Pageable pageable) {
        return accountRepository.findAll(pageable).map(AccountResponse::from);
    }

    @Transactional
    public AccountResponse update(UUID id, UpdateAccountRequest request) {
        Account account = findOrThrow(id);
        account.setName(request.name());
        return AccountResponse.from(account);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = findOrThrow(id);
        accountRepository.delete(account);
    }

    private Account findOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
