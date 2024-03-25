package com.cz.core.consumer;

import com.cz.core.annotation.czConsumer;
import com.cz.core.connect.LoadBalancer;
import com.cz.core.connect.Router;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import com.cz.core.context.RpcContext;
import com.cz.core.registry.RegistryCenter;
import lombok.Data;
import org.springframework.beans.BeansException;
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

    ApplicationContext applicationContext;
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动消费者
     * 收集目前使用到的提供者服务信息
     */
    public void start() {
        // 获取负载均衡信息
        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);
        // TODO 添加 filter 链
        rpcContext.setFilters(new ArrayList<>());

        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        // 获取提供者服务信息
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = findAnnotatedField(bean.getClass());
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
        }
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
        String serviceName = service.getCanonicalName();
        List<String> providerUrls = transRegistryDataToHttp(registryCenter.fetchAll(serviceName));

        System.out.println("before subscribe....");
        providerUrls.forEach(System.out::println);

        registryCenter.subscribe(serviceName, event -> {
            providerUrls.clear();
            providerUrls.addAll(transRegistryDataToHttp(event.getData()));
        });
        return ConsumerProxyFactory.createByJDK(service, rpcContext, providerUrls);
    }

    private List<String> transRegistryDataToHttp(List<String> nodes) {
        return nodes.stream()
                .map(x -> "http://" + x.replace('_', ':')).toList();
    }

    /**
     * 查找被注解的字段
     *
     * @param clazz 类
     * @return List<Field>
     */
    private List<Field> findAnnotatedField(Class<?> clazz) {
        // 取到的是被代理增强的子类
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            List<Field> fieldInCurrentClass = Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(czConsumer.class))
                    .toList();
            result.addAll(fieldInCurrentClass);
            clazz = clazz.getSuperclass();
        }
        return result;
    }


}
