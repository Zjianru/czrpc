package com.cz.core.registry;

import com.cz.core.registry.listener.ChangedListener;

import java.util.List;

/**
 * code desc
 *
 * @author Zjianru
 */
public interface RegistryCenter {
    void start();

    void stop();

    // provider 侧
    void register(String service, String instance);

    void unRegister(String service, String instance);

    // consumer 侧
    List<String> fetchAll(String service);

    void subscribe(String service, ChangedListener listener);

    class StaticRegistryCenter implements RegistryCenter {
        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
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
        public void register(String service, String instance) {
            System.out.println("RegistryCenter register");
        }

        @Override
        public void unRegister(String service, String instance) {
            System.out.println("RegistryCenter unRegister");
        }

        @Override
        public List<String> fetchAll(String service) {
            System.out.println("RegistryCenter fetchAll");
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {
            System.out.println("RegistryCenter subscribe");
        }
    }
}
