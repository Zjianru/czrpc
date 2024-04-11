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
     * 前置处理器
     * 检测当前线程的上下文中是否存在待传递参数
     *
     * @param request 请求
     * @return 过滤器处理结果
     */
    @Override
    public Object perProcess(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    /**
     * 后置处理器
     * 清除掉当前线程上下文中的参数，防止内存泄露和上下文污染
     *
     * @param request  请求
     * @param response 响应
     * @param result   处理结果
     * @return 过滤器处理结果
     */
    @Override
    public Object postProcess(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.ContextParameters.get().clear();
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
