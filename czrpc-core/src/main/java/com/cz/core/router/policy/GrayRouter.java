package com.cz.core.router.policy;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.router.Router;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由
 * 可实现灰度用户 或对一次请求上加灰度标记
 *
 * @author Zjianru
 */
public class GrayRouter implements Router<InstanceMeta> {

    private final int grayRadio;
    private final Random random = new Random();

    public GrayRouter(int grayRadio) {
        this.grayRadio = grayRadio;
    }

    /**
     * 根据流量调拨参数控制节点路由 实现流控
     *
     * @param providers 提供者 instance 信息
     * @return 可实现调用的提供者
     */
    @Override
    public List<InstanceMeta> selectRoute(List<InstanceMeta> providers) {
        LinkedList<InstanceMeta> normalNodes = new LinkedList<>();
        LinkedList<InstanceMeta> grayNodes = new LinkedList<>();
        // 边界处理
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        providers.forEach(provider -> {
            if ("true".equals(provider.getParams().getOrDefault("gray", "false"))) {
                grayNodes.add(provider);
            } else {
                normalNodes.add(provider);
            }
        });

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }
        if (grayRadio <= 0) {
            return normalNodes;
        } else if (grayRadio >= 100) {
            return grayNodes;
        }

        // 流控实现方式一：组成一个有 100 个节点的大集合，灰度节点和正常节点按比例填充，
        // 前提：该方式要求 LB 为线性均匀的，流量能够平均的打到节点上
        // 流控实现方式二：加入一个随机数，该方式不依赖 LB 的策略，且遵循无状态的原则，公平的选取节点
        // 小基数看不出来平均 需要进行大基数测试，10000 次起
        if (random.nextInt(100) < grayRadio) {
            return grayNodes;
        } else {
            return normalNodes;
        }
    }
}
