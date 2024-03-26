package com.cz.core.loadBalance.policy;

import com.cz.core.loadBalance.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * 负载均衡策略 - random - 随机
 *
 * @author Zjianru
 */
public class RandomLoadBalancer<T> implements LoadBalancer<T> {
    Random random = new Random();

    /**
     * random load balance
     *
     * @param providers 提供者
     * @return 选中结果
     */
    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get(random.nextInt(providers.size()));
    }
}
