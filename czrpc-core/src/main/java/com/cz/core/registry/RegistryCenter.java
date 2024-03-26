package com.cz.core.registry;

import com.cz.core.meta.InstanceMeta;
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
    void register(String service, InstanceMeta instance);

    void unRegister(String service, InstanceMeta instance);

    // consumer 侧
    List<InstanceMeta> fetchAll(String service);

    void subscribe(String service, ChangedListener listener);

    /**
     * 静态的默认实现
     */
    class DefaultRegistryCenter implements RegistryCenter {
        List<InstanceMeta> providers;

        public DefaultRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        public void start() {
            System.out.println("RegistryCenter start");
        }

        @Override
        public void stop() {
            System.out.println("RegistryCenter stop");
        }

        @Override
        public void register(String service, InstanceMeta instance) {
            System.out.println("RegistryCenter register");
        }

        @Override
        public void unRegister(String service, InstanceMeta instance) {
            System.out.println("RegistryCenter unRegister");
        }

        @Override
        public List<InstanceMeta> fetchAll(String service) {
            System.out.println("RegistryCenter fetchAll");
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {
            System.out.println("RegistryCenter subscribe");
        }
    }
}
