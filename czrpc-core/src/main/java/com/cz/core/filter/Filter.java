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
    Object perProcess(RpcRequest request);

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @return 过滤器处理结果
     */
    Object postProcess(RpcRequest request, RpcResponse response, Object result);

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
        public Object perProcess(RpcRequest request) {
            return null;
        }

        @Override
        public Object postProcess(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }

        @Override
        public Filter next() {
            return null;
        }
    };
}
