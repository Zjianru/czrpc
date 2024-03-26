package com.cz.core.filter;

import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;

/**
 * 过滤 - 前置或后置处理
 *
 * @author Zjianru
 */
public interface Filter {


    /**
     * 过滤器
     *
     * @param request 请求
     * @return 过滤器处理结果
     */
    RpcResponse perProcess(RpcRequest request);

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @return 过滤器处理结果
     */
    RpcResponse postProcess(RpcRequest request, RpcResponse response);

    /**
     * 下一个过滤器
     *
     * @return filter
     */
    Filter next();

    /**
     * 默认过滤器 - 不做任何处理
     */
    Filter DefaultFilter = new Filter() {
        @Override
        public RpcResponse perProcess(RpcRequest request) {
            return null;
        }

        @Override
        public RpcResponse postProcess(RpcRequest request, RpcResponse response) {
            return response;
        }

        @Override
        public Filter next() {
            return null;
        }
    };
}
