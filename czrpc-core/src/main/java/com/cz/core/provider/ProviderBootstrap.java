package com.cz.core.provider;

import com.alibaba.fastjson2.JSON;
import com.cz.core.annotation.czProvider;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.meta.ProviderMeta;
import com.cz.core.register.RegistryCenter;
import com.cz.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * 提供者注册逻辑
 *
 * @author Zjianru
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    /**
     * 容器上下文
     */
    ApplicationContext applicationContext;

    /**
     * 注册中心信息
     */
    private RegistryCenter registryCenter;

    /**
     * 提供者实例信息
     */
    private String instance;

    /**
     * 端口信息
     */
    @Value("${server.port}")
    private String port;

    /**
     * 服务提供者注册表 存储接口中方法级别的元数据
     * key: service interface
     * value: 接口中的所有自定义方法
     */
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    // issue #1
//    private Map<String, Object> skeleton = new HashMap<>();


    /**
     * 启动时完成 provider 注册
     * 装配目前的 provider 信息
     */
    @PostConstruct
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(czProvider.class);
        registryCenter = applicationContext.getBean(RegistryCenter.class);
        providers.values().forEach(this::getInterface);
    }

    /**
     * 服务下线
     */
    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unRegisterService);
    }

    /**
     * 注册服务到 zookeeper
     */
    @SneakyThrows
    public void start() {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        instance = hostAddress + "_" + port;
        skeleton.keySet().forEach(this::registerService);
        registryCenter.start();
    }


    /**
     * 服务与实例下线
     *
     * @param serviceInfo 服务信息
     */
    private void unRegisterService(String serviceInfo) {
        registryCenter.unRegister(serviceInfo, instance);

    }

    /**
     * 向注册中心注册服务与实例
     *
     * @param serviceInfo 服务信息
     */
    private void registerService(String serviceInfo) {
        registryCenter.register(serviceInfo, instance);
    }

    /**
     * 完成 RPC 调用
     *
     * @param request RPC 请求信息
     * @return RPC 返回数据
     */
    public RpcResponse invoke(RpcRequest request) {
        String methodSign = request.getMethodSign();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService().getCanonicalName());
        Class<?>[] argsType = request.getArgsType();
        Object[] args = request.getArgs();
        // issue #1
//        Method method = findMethod(bean.getClass(), request.getMethod(), argsType);
        RpcResponse response = new RpcResponse();
        try {
            ProviderMeta meta = findProviderMeta(methodSign, providerMetas);
            Method method = meta.getMethod();
            if (argsType == null) {
                argsType = method.getParameterTypes();
            }
            Object[] realArgs = new Object[argsType.length];
            for (int i = 0; i < argsType.length; i++) {
                Object realArg = JSON.to(argsType[i], args[i]);
                realArgs[i] = realArg;
            }


            Object result = method.invoke(meta.getTargetService(), realArgs);
            response.setStatus(true);
            response.setData(result);
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            response.setStatus(false);
            // 传播异常信息
            response.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            response.setStatus(false);
            response.setException(new RuntimeException(e.getMessage()));
        }
        return response;
    }

    /**
     * 查找元数据
     *
     * @param methodSign    方法签名
     * @param providerMetas skeleton 桩子中的元数据链（记录信息为 method 级别）
     * @return ProviderMeta
     */
    private ProviderMeta findProviderMeta(String methodSign, List<ProviderMeta> providerMetas) {
        return providerMetas.stream()
                .filter(meta -> meta.getMethodSign().equals(methodSign))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("no such method"));
    }

    /**
     * 获取接口信息
     *
     * @param bean tagged service
     */
    private void getInterface(Object bean) {
        Class<?> anInterface = bean.getClass().getInterfaces()[0];
        Method[] methods = anInterface.getMethods();
        for (Method method : methods) {
            if (MethodUtils.isLocalMethod(method)) {
                continue;
            }
            createProvider(anInterface, bean, method);
        }
//        skeleton.put(anInterface.getCanonicalName(), bean);
        // 打印 skeleton 当前的注册信息
        System.out.println("current skeleton size is -->" + skeleton.size());
        for (String s : skeleton.keySet()) {
            System.out.println("skeleton key = " + s + " value = " + skeleton.get(s));
        }

    }


    /**
     * 装配元信息
     *
     * @param anInterface key's resource
     * @param bean        service bean
     * @param method      service method
     */
    private void createProvider(Class<?> anInterface, Object bean, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setTargetService(bean);
        providerMeta.setMethodSign(MethodUtils.methodSign(method));
        skeleton.add(anInterface.getCanonicalName(), providerMeta);
    }

    /**
     * 根据参数类型查找方法
     *
     * @param aClass     service
     * @param methodName 方法名
     * @param argsType   参数类型
     * @return declare method
     */
    private Method findMethod(Class<?> aClass, String methodName, Class<?>[] argsType) {
        try {
            return aClass.getMethod(methodName, argsType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
