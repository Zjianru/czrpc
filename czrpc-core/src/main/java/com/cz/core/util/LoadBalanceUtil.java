package com.cz.core.util;

import com.cz.core.enhance.LoadBalancer;
import com.cz.core.enhance.Router;

import java.util.List;

/**
 * 负载均衡和路由工具类
 *
 * @author Zjianru
 */
public class LoadBalanceUtil {

    /**
     * 根据路由策略和负载均衡策略选择一个提供者
     *
     * @param router       路由策略
     * @param loadBalancer 负载均衡策略
     * @param providers    待选举的请求者信息
     * @return 被选中的提供者的请求地址
     */
    public static String chooseProvider(Router router, LoadBalancer loadBalancer, List<String> providers) {
        List<String> route = router.selectRoute(providers);
        String choose = (String) loadBalancer.choose(route);
        System.out.println("finally load balance choose is ===> " + choose);
        return choose;
    }

}
