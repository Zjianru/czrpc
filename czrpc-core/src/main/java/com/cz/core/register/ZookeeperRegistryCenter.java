package com.cz.core.register;

import java.util.List;

/**
 * code desc
 *
 * @author Zjianru
 */
public class ZookeeperRegistryCenter implements RegistryCenter {
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
    public void register(String service, String instance) {

    }

    /**
     * @param service
     * @param instance
     */
    @Override
    public void unRegister(String service, String instance) {

    }

    /**
     * @param service
     * @return
     */
    @Override
    public List<String> fetchAll(String service) {
        return null;
    }

    /**
     * @param service
     */
    @Override
    public void subscribe(String service) {

    }
}
