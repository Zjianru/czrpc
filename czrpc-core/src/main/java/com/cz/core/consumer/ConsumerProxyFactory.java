package com.cz.core.consumer;

import java.lang.reflect.Proxy;

/**
 * 消费者代理工厂
 *
 * @author Zjianru
 */
public class ConsumerProxyFactory {
    public static Object create(Class<?> service) {
        return Proxy.newProxyInstance(
                service.getClassLoader(), new Class[]{service}, new ConsumerProxyByJdk(service)
        );
    }
}
