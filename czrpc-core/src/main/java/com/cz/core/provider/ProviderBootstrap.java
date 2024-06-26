package com.cz.core.provider;

import com.cz.core.annotation.CzProvider;
import com.cz.core.config.AppProperties;
import com.cz.core.config.provider.ProviderProperties;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ProviderMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 提供者注册逻辑
 *
 * @author Zjianru
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {


    /**
     * 容器上下文
     */
    private ApplicationContext applicationContext;
    /**
     * 注册中心信息
     */
    private RegistryCenter registryCenter;
    /**
     * 端口信息
     */
    private String port;
    /**
     * 应用信息
     */
    private AppProperties appProperties;
    /**
     * 提供者信息
     */
    private ProviderProperties providerProperties;
    /**
     * 服务提供者注册表 存储接口中方法级别的元数据
     * key: serviceMeta interface
     * value: 接口中的所有自定义方法
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    /**
     * 提供者实例信息
     */
    private InstanceMeta instance;


    /**
     * constructor
     *
     * @param port               port
     * @param appProperties      appProperties
     * @param providerProperties providerProperties
     */
    public ProviderBootstrap(String port, AppProperties appProperties,
                             ProviderProperties providerProperties) {
        this.port = port;
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    /**
     * 启动时完成 provider 注册
     * 装配目前的 provider 信息
     */
    @PostConstruct
    public void init() {
        registryCenter = applicationContext.getBean(RegistryCenter.class);
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CzProvider.class);
        providers.values().forEach(this::getInterface);
    }

    /**
     * 注册服务到 zookeeper
     */
    @SneakyThrows
    public void start() {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(hostAddress, Integer.valueOf(port))
                .addParams(providerProperties.getMetas());
        // 先启动客户端 后注册服务
        registryCenter.start();
        skeleton.keySet().forEach(this::registerService);
    }

    /**
     * 服务下线
     */
    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unRegisterService);
    }

    /**
     * 向注册中心注册服务与实例
     *
     * @param serviceInfo 服务信息
     */
    private void registerService(String serviceInfo) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .serviceName(serviceInfo)
                .applicationId(appProperties.getApplicationId())
                .env(appProperties.getEnv())
                .nameSpace(appProperties.getNameSpace())
                .build();
        registryCenter.register(serviceMeta, instance);
    }

    /**
     * 服务与实例下线
     *
     * @param serviceInfo 服务信息
     */
    private void unRegisterService(String serviceInfo) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .serviceName(serviceInfo)
                .applicationId(appProperties.getApplicationId())
                .env(appProperties.getEnv())
                .nameSpace(appProperties.getNameSpace())
                .build();
        // 先注销实例 后销毁客户端
        registryCenter.unRegister(serviceMeta, instance);
    }

    /**
     * 获取接口信息
     *
     * @param bean tagged serviceMeta
     */
    private void getInterface(Object bean) {
        Arrays.stream(bean.getClass().getInterfaces()).forEach(service -> {
            Arrays.stream(service.getMethods())
                    .filter(method -> !MethodUtils.isLocalMethod(method))
                    .forEach(method -> createProvider(service, bean, method));
        });
    }

    /**
     * 装配元信息
     *
     * @param anInterface key's resource
     * @param bean        serviceMeta bean
     * @param method      serviceMeta method
     */
    private void createProvider(Class<?> anInterface, Object bean, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder()
                .method(method)
                .targetService(bean)
                .methodSign(MethodUtils.methodSign(method))
                .build();
        skeleton.add(anInterface.getCanonicalName(), providerMeta);
    }

}
