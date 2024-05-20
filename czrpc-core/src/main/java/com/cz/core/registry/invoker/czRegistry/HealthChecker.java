package com.cz.core.registry.invoker.czRegistry;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HealthChecker类用于定时检查服务提供者和消费者的状态。
 *
 * @author Zjianru
 */
@Slf4j
public class HealthChecker {

    // 服务提供者健康检查的定时任务执行器
    private ScheduledExecutorService providerHealthChecker = null;
    // 服务消费者健康检查的定时任务执行器
    private ScheduledExecutorService consumerHealthChecker = null;

    /**
     * 启动健康检查器，初始化服务提供者和消费者健康检查的定时任务执行器。
     */
    public void start() {
        log.info(" [czRegistry] : start with health checker.");
        providerHealthChecker = Executors.newScheduledThreadPool(1);
        consumerHealthChecker = Executors.newScheduledThreadPool(1);
    }

    /**
     * 停止健康检查器，优雅地关闭服务提供者和消费者健康检查的定时任务执行器。
     */
    public void stop() {
        log.info(" [czRegistry] : stop with health checker.");
        gracefulShutdown(providerHealthChecker);
        gracefulShutdown(consumerHealthChecker);
    }

    /**
     * 为消费者启动定时健康检查任务。
     *
     * @param callback 检查任务的回调接口，实现此接口以提供具体的检查逻辑。
     */
    public void consumerCheck(Callback callback) {
        consumerHealthChecker.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 1, 5, TimeUnit.SECONDS); // 每隔1秒开始检查，之后每5秒重复检查一次
    }

    /**
     * 为服务提供者启动定时健康检查任务。
     *
     * @param callback 检查任务的回调接口，实现此接口以提供具体的检查逻辑。
     */
    public void providerCheck(Callback callback) {
        providerHealthChecker.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS); // 每隔5秒开始检查，之后每5秒重复检查一次
    }

    /**
     * 优雅地关闭指定的定时任务执行器。
     *
     * @param executor 需要关闭的定时任务执行器。
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

    /**
     * 健康检查回调接口，用于定义健康检查的具体逻辑。
     */
    public interface Callback {
        void call() throws Exception;
    }

}
