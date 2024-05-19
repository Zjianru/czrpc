package com.cz.core.registry.invoker;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.listener.ChangedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

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

    /**
     * 启动 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void start() {
        log.info("start czRegistry with server:{}", servers); // 记录启动时的服务器列表信息
    }

    /**
     * 停止 czRegistry 服务
     * 该方法没有参数和返回值
     */
    @Override
    public void stop() {
        log.info("stop czRegistry with server:{}", servers); // 记录停止时的服务器列表信息
    }

    /**
     * @param service
     * @param instance
     */
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info("register service:{} with instance:{}", service, instance);


    }

    /**
     * @param service
     * @param instance
     */
    @Override
    public void unRegister(ServiceMeta service, InstanceMeta instance) {

    }

    /**
     * @param service
     * @return
     */
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        return List.of();
    }

    /**
     * @param service
     * @param listener
     */
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {

    }
}
