package com.cz.core.context;

import com.cz.core.filter.Filter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.router.Router;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * rpc 上下文
 *
 * @author Zjianru
 */
@Data
@Builder
public class RpcContext {

    /**
     * 路由
     */
    Router<InstanceMeta> router;

    /**
     * 负载均衡器
     */
    LoadBalancer<InstanceMeta> loadBalancer;

    /**
     * 过滤器
     */
    List<Filter> filters;

    /**
     * 服务元数据参数
     */
    Map<String, String> params;

    /**
     * 重试次数
     */
    int retries;

    /**
     * 重试超时阈值
     */
    int retryTimeout;
}
