package com.compass.bank.account;

import com.compass.bank.common.exception.InsufficientBalanceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Conta bancaria. Concentra as regras de domínio de credito e debito de saldo,
 * mantendo as invariantes (saldo nunca negativo) proximas dos dados.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Controle de concorrencia otimista. Mesmo usando lock pessimista na
     * transferencia, o @Version serve como segunda barreira contra escritas
     * concorrentes por outros caminhos.
     */
    @Version
    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Account() {
        // exigido pelo JPA
    }

    public Account(String name, BigDecimal initialBalance) {
        this.name = name;
        this.balance = initialBalance;
    }

    /** Credita um valor positivo no saldo. */
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * Debita um valor do saldo, recusando caso o saldo seja insuficiente.
     *
     * @throws InsufficientBalanceException quando o saldo e menor que o valor.
     */
    public void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(this.id, this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
