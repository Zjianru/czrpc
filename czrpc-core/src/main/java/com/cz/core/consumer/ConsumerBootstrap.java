package com.cz.core.consumer;

import com.cz.core.annotation.czConsumer;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import com.cz.core.context.RpcContext;
import com.cz.core.enhance.LoadBalancer;
import com.cz.core.enhance.Router;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.util.MethodUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.*;

/**
 * consumer 启动并完成注册
 *
 * @author Zjianru
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    /**
     * 容器上下文
     */
    ApplicationContext applicationContext;

    /**
     * 容器环境信息
     */
    Environment environment;

    /**
     * 端口信息
     */
    @Value("${server.port}")
    private String port;

    /**
     * 应用标识
     */
    @Value("${czrpc.id}")
    private String applicationId;

    /**
     * 命名空间
     */
    @Value("${czrpc.namespace}")
    private String nameSpace;

    /**
     * 环境信息
     */
    @Value("${czrpc.env}")
    private String env;

    /**
     * 注册中心信息
     */
    private Map<String, Object> stub = new HashMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动消费者
     * 收集目前使用到的提供者服务信息
     */
    public void start() {
        // 获取负载均衡信息
        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RpcContext rpcContext = RpcContext.builder()
                .filters(new ArrayList<>())
                .loadBalancer(loadBalancer)
                .router(router)
                .build();
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        // 获取提供者服务信息
        String[] names = applicationContext.getBeanDefinitionNames();
        Arrays.stream(names)
                .map(name -> applicationContext.getBean(name))
                .forEach(bean -> {
                    List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), czConsumer.class);
                    fields.forEach(field -> {
                        try {
                            Class<?> service = field.getType();
                            String serviceName = service.getCanonicalName();
                            Object consumer = stub.get(serviceName);
                            if (consumer == null) {
                                consumer = createFromRegistry(service, rpcContext, registryCenter);
                                stub.put(serviceName, consumer);
                            }
                            field.setAccessible(true);
                            field.set(bean, consumer);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                });
    }

    /**
     * 通过注册中心完成获取 provider 的信息
     *
     * @param service        服务
     * @param rpcContext     rpc 上下文
     * @param registryCenter 注册中心
     * @return Object
     */
    private Object createFromRegistry(Class<?> service, RpcContext rpcContext, RegistryCenter registryCenter) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .serviceName(service.getCanonicalName())
                .applicationId(applicationId)
                .env(env)
                .nameSpace(nameSpace)
                .build();
        List<InstanceMeta> providerUrls = registryCenter.fetchAll(serviceMeta);
        registryCenter.subscribe(serviceMeta, event -> {
            providerUrls.clear();
            providerUrls.addAll(event.getData());
        });
        return ConsumerProxyFactory.createByJDK(service, rpcContext, providerUrls);
    }
}
