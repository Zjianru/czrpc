package com.cz.core.registry.invoker.czRegistry;

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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * czRegistry 注册中心实现
 *
 * @author Zjianru
 */
@Slf4j
@Component
public class CzRegistryCenter implements RegistryCenter {

    @Value("${czrpc.czregistry.servers}")
    private String servers;

    private final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    private final MultiValueMap<InstanceMeta, ServiceMeta> needCheck = new LinkedMultiValueMap<>();

    HealthChecker healthChecker = new HealthChecker();

    public CzRegistryCenter() {
    }

    /**
     * 启动 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void start() {
        healthChecker.start(); // 启动健康检查器
        providerCheck(); // 检查提供者
        log.info("start czRegistry with server:{}", servers); // 记录启动时的服务器列表信息
    }

    /**
     * 停止 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void stop() {
        healthChecker.stop(); // 停止健康检查器
        log.info("stop czRegistry with server:{}", servers); // 记录停止时的服务器列表信息
    }


    /**
     * 注册服务实例
     *
     * @param service  服务元数据，包含服务名称等信息
     * @param instance 服务实例元数据，包含实例的IP、端口等信息
     */
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        try {
            log.info("register service:{} with instance:{}", service, instance);
            String path = path(ApiContext.register.name(), List.of(service));
            Channel.httpPost(path, instance.dataToJson(), null);
            needCheck.add(instance, service);
            log.info("register service:{} with instance:{} success", service, instance);
        } catch (Exception e) {
            log.error("Failed to register service: {}", service, e);
        }
    }

    /**
     * 取消注册服务实例
     *
     * @param service  服务元数据
     * @param instance 服务实例元数据
     */
    @Override
    public void unRegister(ServiceMeta service, InstanceMeta instance) {
        try {
            log.info("unRegister service:{} with instance:{}", service, instance);
            String path = path(ApiContext.unRegister.name(), List.of(service));
            Channel.httpPost(path, instance.dataToJson(), Void.class);
            needCheck.remove(instance, service);
            log.info("unRegister service:{} with instance:{} success", service, instance);
        } catch (Exception e) {
            log.error("Failed to unregister service: {}", service, e);
        }
    }

    /**
     * 获取指定服务的所有实例
     *
     * @param service 服务元数据
     * @return 返回该服务的所有实例列表
     */
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        try {
            log.info("fetchAll service:{} ", service);
            String path = path(ApiContext.fetchAll.name(), List.of(service));
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
     * 订阅服务变化事件
     *
     * @param service  服务元数据
     * @param listener 服务变化事件监听器
     */
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerCheck(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            String path = path(ApiContext.version.name(), List.of(service));
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
        });
    }


    /**
     * 构建请求路径
     *
     * @param context     请求上下文
     * @param serviceList 服务元数据列表
     * @return 返回构建好的请求路径
     */
    private String path(String context, List<ServiceMeta> serviceList) {
        String services = String.join(",", serviceList.stream().map(ServiceMeta::toPath).toList());
        return servers + context + "?services=" + services;
    }

    /**
     * 检查服务提供者
     */
    public void providerCheck() {
        healthChecker.providerCheck(() -> needCheck.keySet().forEach(instance -> {
                    String path = path(ApiContext.reNews.name(), needCheck.get(instance));
                    log.info("reNews start ======> path is {} , instance is {}", path, instance);
                    Channel.httpPost(path, JSON.toJSONString(instance), Long.class);
                    log.info("reNews end ====== ");
                }
        ));
    }

}
