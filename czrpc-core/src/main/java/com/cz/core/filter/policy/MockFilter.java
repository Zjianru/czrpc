package com.cz.core.filter.policy;

import com.cz.core.filter.Filter;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.util.MethodUtils;
import com.cz.core.util.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 挡板拦截器
 *
 * @author Zjianru
 */
public class MockFilter implements Filter {

    /**
     * 过滤器
     *
     * @param request 请求
     * @return 过滤器处理结果
     */
    @Override
    @SneakyThrows
    public Object perProcess(RpcRequest request) {
        Class<?> service = Class.forName(String.valueOf(request.getService()));
        Method method = findMethod(service, request.getMethodSign());
        Class<?> clazz = method.getReturnType();
        return MockUtils.mock(clazz);
    }

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @return 过滤器处理结果
     */
    @Override
    public Object postProcess(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }

    /**
     * 下一个过滤器
     *
     * @return filter
     */
    @Override
    public Filter next() {
        return null;
    }


    private Method findMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.isLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst().orElse(null);
    }

}
