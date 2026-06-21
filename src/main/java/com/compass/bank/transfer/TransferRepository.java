package com.compass.bank.transfer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    /** Recupera uma transferencia previa pela chave de idempotencia. */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    /**
     * Movimentacoes (extrato) de uma conta: todas as transferencias em que ela
     * participa como origem ou destino, mais recentes primeiro.
     */
    @Query("""
            select t from Transfer t
            where t.sourceAccount.id = :accountId
               or t.destinationAccount.id = :accountId
            order by t.createdAt desc
            """)
    Page<Transfer> findMovements(@Param("accountId") UUID accountId, Pageable pageable);
}
