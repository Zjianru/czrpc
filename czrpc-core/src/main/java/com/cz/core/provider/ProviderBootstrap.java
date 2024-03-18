package com.cz.core.provider;

import com.cz.core.annotation.czProvider;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 提供者注册逻辑
 *
 * @author Zjianru
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();


    @PostConstruct
    public void scanProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(czProvider.class);
        providers.values().forEach(this::getInterface);
    }
    public RpcResponse invoke(RpcRequest request) {
        Object bean = skeleton.get(request.getService().getCanonicalName());
        try {
            Method method = findMethod(bean.getClass(),request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse<>(true, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void getInterface(Object bean) {
        Class<?> anInterface = bean.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), bean);
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
