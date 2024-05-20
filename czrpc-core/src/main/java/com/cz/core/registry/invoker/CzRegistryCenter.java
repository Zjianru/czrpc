package com.cz.core.registry.invoker;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.Event;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.channel.Channel;
import com.cz.core.registry.listener.ChangedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * czRegistry 注册中心实现
 *
 * @author Zjianru
 */
@Slf4j
public class CzRegistryCenter implements RegistryCenter {

    @Value("${czrpc.czregistry.servers}")
    private String servers;

    private final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    private ScheduledExecutorService providerHealthChecker = null;
    private ScheduledExecutorService consumerHealthChecker = null;
    private ScheduledExecutorService subscribeExecutor = null;
    private final MultiValueMap<InstanceMeta, ServiceMeta> needCheck = new LinkedMultiValueMap<>();

    public CzRegistryCenter() {
    }

    /**
     * 启动 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void start() {
        providerHealthChecker = Executors.newScheduledThreadPool(1);
        consumerHealthChecker = Executors.newScheduledThreadPool(1);
        subscribeExecutor = Executors.newScheduledThreadPool(1);
        providerHealthChecker.scheduleAtFixedRate(() -> {
                    needCheck.keySet().forEach(instance -> {
                        String service = String.join(",", needCheck.get(instance).stream().map(ServiceMeta::toPath).toList());
                        String path = servers + "/reNews?service=" + service;
                        log.info("reNews start ======> path is {} , instance is {}", path, instance);
                        Channel.httpPost(path, JSON.toJSONString(instance), Long.class);
                        log.info("reNews end ====== ");
                    });
                },
                5000, 5000, TimeUnit.MILLISECONDS);

        log.info("start czRegistry with server:{}", servers); // 记录启动时的服务器列表信息
    }

    /**
     * 停止 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void stop() {
        gracefulShutdown(providerHealthChecker);
        gracefulShutdown(consumerHealthChecker);
        gracefulShutdown(subscribeExecutor);
        log.info("stop czRegistry with server:{}", servers); // 记录停止时的服务器列表信息
    }

    /**
     * 优雅停机
     */
    private void gracefulShutdown(ScheduledExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册服务实例。
     *
     * @param service  服务元数据，包含服务名称等信息。
     * @param instance 服务实例元数据，包含实例的IP、端口等信息。
     */
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        try {
            log.info("register service:{} with instance:{}", service, instance);
            String path = servers + "/register?service=" + service.toPath();
            Channel.httpPost(path, instance.dataToJson(), null);
            needCheck.add(instance, service);
            log.info("register service:{} with instance:{} success", service, instance);
        } catch (Exception e) {
            log.error("Failed to register service: {}", service, e);
        }
    }

    /**
     * 取消注册服务实例。
     *
     * @param service  服务元数据。
     * @param instance 服务实例元数据。
     */
    @Override
    public void unRegister(ServiceMeta service, InstanceMeta instance) {
        try {
            log.info("unRegister service:{} with instance:{}", service, instance);
            String path = servers + "/unregister?service=" + service.toPath();
            Channel.httpPost(path, instance.dataToJson(), Void.class);
            needCheck.remove(instance, service);
            log.info("unRegister service:{} with instance:{} success", service, instance);
        } catch (Exception e) {
            log.error("Failed to unregister service: {}", service, e);
        }
    }

    /**
     * 获取指定服务的所有实例。
     *
     * @param service 服务元数据。
     * @return 返回该服务的所有实例列表。
     */
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        try {
            log.info("fetchAll service:{} ", service);
            String path = servers + "/fetchAll?service=" + service.toPath();
            List<InstanceMeta> instanceMeta = Channel.httpGet(path, new TypeReference<>() {
            });
            log.info("fetchAll service:{}  success, response is {}", service, instanceMeta);
            return instanceMeta;
        } catch (Exception e) {
            log.error("Failed to fetch all instances of service: {}", service, e);
            return new ArrayList<>(); // 根据实际情况考虑是否返回空列表或抛出运行时异常
        }
    }

    /**
     * 订阅服务变化事件。
     *
     * @param service  服务元数据。
     * @param listener 服务变化事件监听器。
     */
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        subscribeExecutor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            String path = servers + "/version?service=" + service.toPath();
            log.info("subscribe service:{} with version:{}", service, version);
            try {
                Long responseVersion = Channel.httpGet(path, Long.class);
                if (responseVersion > version) {
                    List<InstanceMeta> instanceMetas = fetchAll(service);
                    listener.fire(new Event(instanceMetas));
                    VERSIONS.put(service.toPath(), responseVersion);
                }
            } catch (Exception e) {
                log.error("Failed to subscribe service: {}", service, e);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
