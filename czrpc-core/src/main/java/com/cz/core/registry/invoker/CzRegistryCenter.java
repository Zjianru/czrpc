package com.cz.core.registry.invoker;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.listener.ChangedListener;

import java.util.List;

/**
 * 对接 czRegistry 注册中心
 *
 * @author Zjianru
 */
public class CzRegistryCenter implements RegistryCenter {
    /**
     *
     */
    @Override
    public void start() {

    }

    /**
     *
     */
    @Override
    public void stop() {

    }

    /**
     * @param service
     * @param instance
     */
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {

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
