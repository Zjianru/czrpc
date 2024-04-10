package com.cz.core.provider;

import com.cz.core.context.RpcContext;
import com.cz.core.ex.ExErrorCodes;
import com.cz.core.ex.RpcException;
import com.cz.core.meta.ProviderMeta;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 实现具体 RPC 调用
 *
 * @author Zjianru
 */
@Slf4j
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
        log.debug(" ===> ProviderInvoker.invoke(request:{})", request);
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }
        String methodSign = request.getMethodSign();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService().getCanonicalName());
        RpcResponse<Object> response = new RpcResponse<>();
        response.setStatus(false);
        try {
            ProviderMeta meta = findProviderMeta(methodSign, providerMetas);
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(meta.getTargetService(), args);
            response.setStatus(true);
            response.setData(result);
        } catch (InvocationTargetException e) {
            // 传播异常信息
            response.setException(new RpcException(e.getTargetException().getMessage(), ExErrorCodes.PROVIDER_ERROR));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            response.setException(new RpcException(e.getMessage(), ExErrorCodes.PROVIDER_ERROR));
        } finally {
            // 防止内存泄露和上下文污染
            RpcContext.ContextParameters.get().clear();
        }
        log.debug(" ===> ProviderInvoker.invoke() = {}", response);
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
                .orElseThrow(() -> new RpcException(ExErrorCodes.PROVIDER_NOT_FOUND));
    }

    /**
     * 处理参数转型
     *
     * @param args                  参数
     * @param parameterTypes        参数类型
     * @param genericParameterTypes 泛型
     * @return 处理后的参数
     */
    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actual = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actual[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actual;
    }

}
