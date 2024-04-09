package com.cz.core.util;

import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.router.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 负载均衡和路由工具类
 *
 * @author Zjianru
 */
@Slf4j
public class LoadBalanceUtil {

    /**
     * 根据路由策略和负载均衡策略选择一个提供者
     *
     * @param router       路由策略
     * @param loadBalancer 负载均衡策略
     * @param providers    待选举的请求者信息
     * @return 被选中的提供者的请求地址
     */
    public static InstanceMeta chooseProvider(Router<InstanceMeta> router, LoadBalancer<InstanceMeta> loadBalancer, List<InstanceMeta> providers) {
        List<InstanceMeta> route = router.selectRoute(providers);
        return loadBalancer.choose(route);
    }

}
