package com.cz.core.filter.policy;

import com.cz.core.context.RpcContext;
import com.cz.core.filter.Filter;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;

import java.util.Map;

/**
 * code desc
 *
 * @author Zjianru
 */
public class RequestParamFilter implements Filter {
    /**
     * 过滤器
     *
     * @param request 请求
     * @return 过滤器处理结果
     */
    @Override
    public Object perProcess(RpcRequest request) {
        return null;
    }

    /**
     * 后置处理
     *
     * @param request  请求
     * @param response 响应
     * @param result
     * @return 过滤器处理结果
     */
    @Override
    public Object postProcess(RpcRequest request, RpcResponse response, Object result) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
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
