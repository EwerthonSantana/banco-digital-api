package com.compass.bank.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> captor;

    @Test
    @DisplayName("Gera uma notificacao para o remetente e outra para o destinatario")
    void onTransferCompleted() {
        UUID transferId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID destId = UUID.randomUUID();
        TransferCompletedEvent event = new TransferCompletedEvent(
                transferId, sourceId, "Maria", destId, "Joao",
                new BigDecimal("30.00"), Instant.now());

        notificationService.onTransferCompleted(event);

        verify(notificationRepository, times(2)).save(captor.capture());
        List<Notification> saved = captor.getAllValues();
        assertThat(saved).extracting(Notification::getType)
                .containsExactlyInAnyOrder(
                        Notification.Type.TRANSFER_SENT,
                        Notification.Type.TRANSFER_RECEIVED);
        assertThat(saved).extracting(Notification::getTransferId)
                .containsOnly(transferId);
    }
}
