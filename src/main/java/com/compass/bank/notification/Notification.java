package com.compass.bank.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Registro de uma notificacao enviada a um cliente. Persistir a notificacao
 * cria uma trilha de auditoria e permite reprocessamento em caso de falha.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    public enum Type {
        TRANSFER_SENT,
        TRANSFER_RECEIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "transfer_id", nullable = false)
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type type;

    @Column(nullable = false, length = 500)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
        // exigido pelo JPA
    }

    public Notification(UUID accountId, UUID transferId, Type type, String message) {
        this.accountId = accountId;
        this.transferId = transferId;
        this.type = type;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
