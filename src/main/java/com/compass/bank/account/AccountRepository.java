package com.compass.bank.account;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Carrega a conta aplicando lock pessimista de escrita
     * (traduzido para {@code SELECT ... FOR UPDATE} no PostgreSQL).
     *
     * <p>Garante que, durante uma transferencia, nenhuma outra transacao
     * consiga ler/alterar o saldo desta conta ate o commit, eliminando
     * condicoes de corrida sobre o saldo.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);
}
