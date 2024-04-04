package com.cz.core.filter.policy;

import com.cz.core.filter.Filter;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.util.MockUtils;

import java.lang.reflect.Method;

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
    public Object perProcess(RpcRequest request) {
        Method method = request.getMethod();
        Class<?> returnType = method.getReturnType();
        // 根据返回值类型  模拟返回值
        return MockUtils.mock(returnType);
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
}
