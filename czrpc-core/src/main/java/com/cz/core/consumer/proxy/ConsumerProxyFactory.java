package com.cz.core.consumer.proxy;

import com.cz.core.consumer.proxy.impl.ProxyByJdk;

import java.lang.reflect.Proxy;

/**
 * 消费者代理工厂
 *
 * @author Zjianru
 */
public class ConsumerProxyFactory {
    // TODO 扩展点 - 多方式动态代理实现
    public static Object createByJDK(Class<?> service) {
        return Proxy.newProxyInstance(
                service.getClassLoader(), new Class[]{service}, new ProxyByJdk(service)
        );
    }
}
