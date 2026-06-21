package com.compass.bank.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Pool de threads dedicado ao envio assincrono de notificacoes.
 *
 * <p>Isolar as notificacoes em seu proprio executor evita que um eventual
 * lentidao no canal de notificacao impacte o throughput das transferencias.</p>
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }
}
