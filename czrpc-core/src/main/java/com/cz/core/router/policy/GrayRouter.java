package com.cz.core.router.policy;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.router.Router;

import java.util.List;

/**
 * 灰度路由
 *
 * @author Zjianru
 */
public class GrayRouter implements Router<InstanceMeta> {

    private final int grayRadio;

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
        return providers;
    }
}
