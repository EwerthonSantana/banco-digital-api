package com.compass.bank.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envia notificacoes apos uma transferencia concluida.
 *
 * <p>Decisoes de resiliencia:</p>
 * <ul>
 *   <li>{@code @TransactionalEventListener(AFTER_COMMIT)} garante que a
 *       notificacao so dispara depois que a transferencia foi efetivamente
 *       persistida; um rollback nao gera notificacao "fantasma".</li>
 *   <li>{@code @Async} executa o envio fora da thread da transferencia, para
 *       nao impactar a latencia/throughput do caminho critico.</li>
 *   <li>A notificacao e persistida (trilha de auditoria) antes do "envio".</li>
 * </ul>
 *
 * <p>O envio em si e simulado por log. Em producao, esta classe chamaria um
 * gateway de e-mail/SMS/push; a interface ja esta isolada aqui.</p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        notifySender(event);
        notifyReceiver(event);
    }

    private void notifySender(TransferCompletedEvent event) {
        String message = String.format(
                "Ola %s, sua transferencia de R$ %s para %s foi concluida com sucesso.",
                event.sourceAccountName(), event.amount(), event.destinationAccountName());
        persistAndDispatch(event.sourceAccountId(), event.transferId(),
                Notification.Type.TRANSFER_SENT, message);
    }

    private void notifyReceiver(TransferCompletedEvent event) {
        String message = String.format(
                "Ola %s, voce recebeu uma transferencia de R$ %s de %s.",
                event.destinationAccountName(), event.amount(), event.sourceAccountName());
        persistAndDispatch(event.destinationAccountId(), event.transferId(),
                Notification.Type.TRANSFER_RECEIVED, message);
    }

    private void persistAndDispatch(java.util.UUID accountId, java.util.UUID transferId,
                                    Notification.Type type, String message) {
        Notification notification = new Notification(accountId, transferId, type, message);
        notificationRepository.save(notification);
        // Simulacao do envio pelo canal externo:
        log.info("[NOTIFICACAO] conta={} tipo={} -> {}", accountId, type, message);
    }
}
