package com.cz.core.consumer;

import com.cz.core.annotation.CzConsumer;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import com.cz.core.context.RpcContext;
import com.cz.core.enhance.Router;
import com.cz.core.filter.Filter;
import com.cz.core.loadBalance.LoadBalancer;
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

import static com.cz.core.filter.Filter.DefaultFilter;

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
                .filters(Collections.singletonList(DefaultFilter))
                .loadBalancer(loadBalancer)
                .router(router)
                .build();
        processFilter(rpcContext, null);

        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        // 获取提供者服务信息
        String[] names = applicationContext.getBeanDefinitionNames();
        Arrays.stream(names)
                .map(name -> applicationContext.getBean(name))
                .forEach(bean -> {
                    List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), CzConsumer.class);
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
                        } catch (IllegalAccessException ignored) {
                        }
                    });
                });
    }

    /**
     * 处理过滤器链
     *
     * @param rpcContext rpc 上下文
     * @param filter     指定的 filter 策略
     */
    private void processFilter(RpcContext rpcContext, Filter filter) {
        List<Filter> list = applicationContext.getBeansOfType(Filter.class).values().stream().toList();
        if (list.isEmpty()) {
            list = new ArrayList<>();
        }
        if (filter == null) {
            filter = DefaultFilter;
        }
        List<Filter> filters = new ArrayList<>(list);
        filters.add(filter);
        rpcContext.setFilters(filters);
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
