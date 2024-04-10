package com.cz.core.consumer;

import com.cz.core.annotation.CzConsumer;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import com.cz.core.context.RpcContext;
import com.cz.core.filter.Filter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.router.Router;
import com.cz.core.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
     * 重试次数
     */
    @Value("${czrpc.retries:1}")
    private int retries;

    /**
     * 重试阈值 - 超时达到此阈值，即进行重试
     */
    @Value("${czrpc.retryTimeout:1000}")
    private int retryTimeout;

    /**
     * 半开探活初始延迟
     */
    @Value("${czrpc.isolate.halfOpen.initialDelay:10000}")
    private long initialDelay;

    /**
     * 半开探活每次间隔，单位 - 毫秒
     */
    @Value("${czrpc.isolate.halfOpen.delay:60000}")
    private long delay;

    /**
     * 请求 30 s 内错误阈值 - 超过此限制即进行故障隔离
     */
    @Value("${czrpc.isolate.faultLimit:1000}")
    private int faultLimit;

    /**
     * 灰度 - 流量调拨比重 0-100
     */
    @Value("${czrpc.metas.grayRadio:10}")
    private int grayRadio;

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
        RpcContext rpcContext = createContext();
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
     * wrapper context 信息
     *
     * @return rpcContext
     */
    private RpcContext createContext() {
        // 获取负载均衡信息
        Router<InstanceMeta> router = applicationContext.getBean(Router.class);
        LoadBalancer<InstanceMeta> loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RpcContext ctx = RpcContext.builder()
                .filters(Collections.singletonList(DefaultFilter))
                .loadBalancer(loadBalancer)
                .router(router)
                .retries(retries)
                .build();
        Map<String, String> params = new HashMap<>();
        ctx.setParams(params);
        // 放置超时重试配置
        params.put("retries.retryTimeout", String.valueOf(retryTimeout));
        // 放置半开探活配置
        params.put("isolate.halfOpen.delay", String.valueOf(delay));
        params.put("isolate.halfOpen.initialDelay", String.valueOf(initialDelay));
        // 灰度 - 流量调拨比重
        params.put("metas.grayRadio", String.valueOf(grayRadio));
        return ctx;
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
