package com.cz.core.filter.policy;

import com.cz.core.filter.Filter;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;

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
    public RpcResponse perProcess(RpcRequest request) {
        return null;
    }

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @return 过滤器处理结果
     */
    @Override
    public RpcResponse postProcess(RpcRequest request, RpcResponse response) {
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
