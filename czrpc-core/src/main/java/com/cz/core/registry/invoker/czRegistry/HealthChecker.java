package com.cz.core.registry.invoker.czRegistry;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * code desc
 *
 * @author Zjianru
 */
@Slf4j
public class HealthChecker {


    private ScheduledExecutorService providerHealthChecker = null;
    private ScheduledExecutorService consumerHealthChecker = null;

    public void start() {
        log.info(" [czRegistry] : start with health checker.");
        providerHealthChecker = Executors.newScheduledThreadPool(1);
        consumerHealthChecker = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
        log.info(" [czRegistry] : stop with health checker.");
        gracefulShutdown(providerHealthChecker);
        gracefulShutdown(consumerHealthChecker);
    }

    public void consumerCheck(Callback callback) {
        consumerHealthChecker.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public void providerCheck(Callback callback) {
        providerHealthChecker.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 优雅停机
     */
    private void gracefulShutdown(ScheduledExecutorService executor) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public interface Callback {
        void call() throws Exception;
    }

}
