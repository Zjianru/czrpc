package com.cz.core.filter.policy;

import com.cz.core.filter.Filter;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * code desc
 *
 * @author Zjianru
 */
@Order
public class CacheFilter implements Filter {
    // TODO 定义cache容量，和过期 替换为 Guava cache
    Map<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * 过滤器
     *
     * @param request 请求
     * @return 过滤器处理结果
     */
    @Override
    public Object perProcess(RpcRequest request) {
        return cache.get(request.toString());
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
        cache.putIfAbsent(request.toString(), result);
        return result;
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
