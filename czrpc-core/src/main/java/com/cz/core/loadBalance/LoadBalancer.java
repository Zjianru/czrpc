package com.cz.core.loadBalance;

import java.util.List;

/**
 * 负载均衡 - 选取合适的节点进行通信
 * random
 * RR
 * weightedRR
 * AAWR 自适应
 * avg * 0.3 + last * 0.7 = WEIGHT*
 *
 * @author Zjianru
 */
public interface LoadBalancer<T> {

    LoadBalancer Default = nodes -> (nodes == null || nodes.isEmpty()) ? null : nodes.get(0);

    T choose(List<T> providers);
}
