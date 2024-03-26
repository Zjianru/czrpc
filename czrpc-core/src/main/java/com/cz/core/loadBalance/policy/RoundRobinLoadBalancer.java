package com.cz.core.loadBalance.policy;

import com.cz.core.loadBalance.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡策略 - Robin 轮询
 *
 * @author Zjianru
 */
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {
    AtomicInteger index = new AtomicInteger(0);

    /**
     * Robin load balance
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
        // 0x7fffffff 保证 index 即使溢出，也会是正数
        return providers.get((index.getAndIncrement() & 0x7fffffff) % providers.size());
    }
}
