package com.cz.core.provider;

import com.alibaba.fastjson2.JSON;
import com.cz.core.meta.ProviderMeta;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 实现具体 RPC 调用
 *
 * @author Zjianru
 */
public class ProviderInvoker {

    /**
     * 服务提供者注册表 存储接口中方法级别的元数据，为 {@link ProviderBootstrap } 中 skeleton 的引用
     * key: service interface
     * value: 接口中的所有自定义方法
     */
    private final MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    /**
     * 完成 RPC 调用
     *
     * @param request RPC 请求信息
     * @return RPC 返回数据
     */
    public RpcResponse<Object> invoke(RpcRequest request) {
        String methodSign = request.getMethodSign();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService().getCanonicalName());
        Class<?>[] argsType = request.getArgsType();
        Object[] args = request.getArgs();
        RpcResponse<Object> response = new RpcResponse<>();
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
}
