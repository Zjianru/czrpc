package com.cz.core.registry.invoker;

import com.alibaba.fastjson2.TypeReference;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.Event;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.channel.HttpChannel;
import com.cz.core.registry.listener.ChangedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${czrpc.czRegistry.servers}")
    private String servers;

    private final HttpChannel channel;
    private final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = null;

    public CzRegistryCenter(HttpChannel channel) {
        this.channel = channel;
    }

    /**
     * 启动 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void start() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        log.info("start czRegistry with server:{}", servers); // 记录启动时的服务器列表信息
    }

    /**
     * 停止 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("stop czRegistry with server:{}", servers); // 记录停止时的服务器列表信息
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
            channel.post(path, instance.dataToJson(), Void.class);
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
            channel.post(path, instance.dataToJson(), Void.class);
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
            List<InstanceMeta> instanceMeta = channel.get(path, new TypeReference<>() {
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
        executor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            String path = servers + "/version?service=" + service.toPath();
            log.info("subscribe service:{} with version:{}", service, version);
            try {
                Long responseVersion = channel.get(path, Long.class);
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
