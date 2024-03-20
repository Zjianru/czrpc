package com.cz.core.consumer.proxy;

import java.lang.reflect.Proxy;

/**
 * 消费者代理工厂
 *
 * @author Zjianru
 */
public class ConsumerProxyFactory {
    // TODO 扩展点 - 多方式动态代理实现
    public static Object create(Class<?> service) {
        return Proxy.newProxyInstance(
                service.getClassLoader(), new Class[]{service}, new ConsumerProxyByJdk(service)
        );
    }
}
