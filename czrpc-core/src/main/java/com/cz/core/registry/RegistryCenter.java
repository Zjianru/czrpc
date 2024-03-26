package com.cz.core.registry;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.listener.ChangedListener;

import java.util.List;

/**
 * 注册中心
 *
 * @author Zjianru
 */
public interface RegistryCenter {
    void start();

    void stop();

    // provider 侧
    void register(ServiceMeta service, InstanceMeta instance);

    void unRegister(ServiceMeta service, InstanceMeta instance);

    // consumer 侧
    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangedListener listener);

    /**
     * 静态的默认实现
     */
    class DefaultRegistryCenter implements RegistryCenter {
        List<InstanceMeta> providers;

        public DefaultRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {
        }

        @Override
        public void unRegister(ServiceMeta service, InstanceMeta instance) {
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {
        }
    }
}
