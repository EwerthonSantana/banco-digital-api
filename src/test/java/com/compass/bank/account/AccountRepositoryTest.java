package com.compass.bank.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("findByIdForUpdate (SELECT ... FOR UPDATE) carrega a conta persistida")
    void findByIdForUpdate() {
        Account saved = accountRepository.save(new Account("Maria", new BigDecimal("100.00")));

        Optional<Account> found = accountRepository.findByIdForUpdate(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Maria");
        assertThat(found.get().getBalance()).isEqualByComparingTo("100.00");
    }
}
