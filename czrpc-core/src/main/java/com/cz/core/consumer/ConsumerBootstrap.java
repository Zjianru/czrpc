package com.cz.core.consumer;

import com.cz.core.annotation.CzConsumer;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import com.cz.core.context.RpcContext;
import com.cz.core.filter.Filter;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
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
@Slf4j
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
        // 初始化 rpcContext
        RpcContext rpcContext = applicationContext.getBean(RpcContext.class);
        // 为 context 处理过滤器
        processFilter(rpcContext, null);
        // 获取注册中心信息
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);
        // 获取提供者服务信息
        String[] names = applicationContext.getBeanDefinitionNames();
        Arrays.stream(names)
                .map(name -> applicationContext.getBean(name))
                .forEach(bean -> {
                    List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), CzConsumer.class);
                    fields.forEach(field -> {
                        Class<?> service = field.getType();
                        String serviceName = service.getCanonicalName();
                        log.info(" ===> {}", field.getName());
                        try {
                            Object consumer = stub.get(serviceName);
                            if (consumer == null) {
                                consumer = createFromRegistry(service, rpcContext, registryCenter);
                                stub.put(serviceName, consumer);
                            }
                            field.setAccessible(true);
                            field.set(bean, consumer);
                        } catch (IllegalAccessException e) {
                            // ignore and print it
                            log.warn(" ==> Field[{}.{}] create consumer failed.", serviceName, field.getName());
                            log.error("Ignore and print it as: ", e);
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
                .applicationId(rpcContext.getDataFromParam("czrpc.applicationId"))
                .env(rpcContext.getDataFromParam("czrpc.env"))
                .nameSpace(rpcContext.getDataFromParam("czrpc.nameSpace"))
                .build();
        List<InstanceMeta> providerUrls = registryCenter.fetchAll(serviceMeta);
        registryCenter.subscribe(serviceMeta, event -> {
            providerUrls.clear();
            providerUrls.addAll(event.getData());
        });
        return ConsumerProxyFactory.createByJDK(service, rpcContext, providerUrls);
    }
}
