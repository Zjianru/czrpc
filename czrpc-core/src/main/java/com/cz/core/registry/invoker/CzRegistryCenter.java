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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 从配置中获取czRegistry服务器列表
     */
    @Value("${czrpc.czRegistry.servers}")
    private String servers;


    private final HttpChannel channel;

    public CzRegistryCenter(HttpChannel channel) {
        this.channel = channel;
    }

    private final Map<String, Long> VERSIONS = new HashMap<>();
    private final ScheduledExecutorService executor = null;

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
        // 日志记录注册服务开始
        log.info("register service:{} with instance:{}", service, instance);
        // 构建注册路径并发送注册请求
        String path = servers + "/register?service=" + service.toPath();
        channel.post(path, instance.dataToJson(), Void.class);
        // 日志记录注册服务成功
        log.info("register service:{} with instance:{} success", service, instance);
    }

    /**
     * 取消注册服务实例。
     *
     * @param service  服务元数据。
     * @param instance 服务实例元数据。
     */
    @Override
    public void unRegister(ServiceMeta service, InstanceMeta instance) {
        // 日志记录取消注册服务开始
        log.info("unRegister service:{} with instance:{}", service, instance);
        // 构建取消注册路径并发送请求
        String path = servers + "/unregister?service=" + service.toPath();
        channel.post(path, instance.dataToJson(), Void.class);
        // 日志记录取消注册服务成功
        log.info("unRegister service:{} with instance:{} success", service, instance);
    }

    /**
     * 获取指定服务的所有实例。
     *
     * @param service 服务元数据。
     * @return 返回该服务的所有实例列表。
     */
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        // 日志记录开始获取所有服务实例
        log.info("fetchAll service:{} ", service);
        // 构建获取所有实例路径并发送请求
        String path = servers + "/fetchAll?service=" + service.toPath();
        List<InstanceMeta> instanceMeta = channel.get(path, new TypeReference<>() {
        });
        // 日志记录获取所有服务实例成功
        log.info("fetchAll service:{}  success, response is {}", service, instanceMeta);
        return instanceMeta;
    }

    /**
     * 订阅服务变化事件。
     *
     * @param service  服务元数据。
     * @param listener 服务变化事件监听器。
     */
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        // 使用定时任务定期查询服务版本并检查更新
        executor.scheduleWithFixedDelay(() -> {
            // 获取当前版本号
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            // 构建查询版本路径并发送请求
            String path = servers + "/version?service=" + service.toPath();
            log.info("subscribe service:{} with version:{}", service, version);
            log.info("send msg to registry ,path is {}", path);
            // 获取服务的最新版本号
            Long responseVersion = channel.get(path, Long.class);
            // 检查版本是否有更新
            if (responseVersion > version) {
                // 如果有更新，则获取所有实例并触发监听器事件
                List<InstanceMeta> instanceMetas = fetchAll(service);
                listener.fire(new Event(instanceMetas));
                // 更新版本信息
                VERSIONS.put(service.toPath(), responseVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

}
