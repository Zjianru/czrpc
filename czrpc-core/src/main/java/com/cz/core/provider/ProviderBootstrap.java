package com.cz.core.provider;

import com.alibaba.fastjson2.JSON;
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
        Class<?>[] argsType = request.getArgsType();
        Object[] args = request.getArgs();
        Method method = findMethod(bean.getClass(), request.getMethod(), argsType);

        Object[] realArgs = new Object[argsType.length];
        for (int i = 0; i < argsType.length; i++) {
            Object realArg = JSON.to(argsType[i], args[i]);
            realArgs[i] = realArg;
        }

        RpcResponse response = new RpcResponse();
        try {
            Object result = method.invoke(bean, realArgs);
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

    private void getInterface(Object bean) {
        Class<?> anInterface = bean.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), bean);
    }

    private Method findMethod(Class<?> aClass, String methodName, Class<?>[] argsType) {
        try {
            return aClass.getMethod(methodName, argsType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
