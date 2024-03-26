package com.cz.core.consumer.proxy;

import com.cz.core.consumer.proxy.invoker.JdkProxyInvoker;
import com.cz.core.context.RpcContext;
import com.cz.core.meta.InstanceMeta;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 消费者代理工厂
 *
 * @author Zjianru
 */
public class ConsumerProxyFactory {
    // TODO 扩展点 - 多方式动态代理实现
    public static Object createByJDK(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providerUrls) {
        return Proxy.newProxyInstance(
                service.getClassLoader(), new Class[]{service}, new JdkProxyInvoker(service, rpcContext, providerUrls)
        );
    }
}
