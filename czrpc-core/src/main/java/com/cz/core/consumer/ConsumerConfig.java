package com.cz.core.consumer;

import com.cz.core.filter.Filter;
import com.cz.core.filter.policy.CacheFilter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.loadBalance.policy.RoundRobinLoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.impl.ZookeeperRegistryCenter;
import com.cz.core.router.Router;
import com.cz.core.router.policy.GrayRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * consumer 配置类
 *
 * @author Zjianru
 */
@Configuration
public class ConsumerConfig {

    @Bean
    @Order(1)
    ConsumerBootstrap createConsumer() {
        return new ConsumerBootstrap();
    }

    /**
     * 初始化消费端
     *
     * @param consumerBootstrap 消费端初始化逻辑
     * @return ApplicationRunner
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerInit(@Autowired ConsumerBootstrap consumerBootstrap) {
        return args -> consumerBootstrap.start();
    }

    /**
     * 加载负载均衡策略
     *
     * @return 负载均衡实现
     */
    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinLoadBalancer<>();
    }

    /**
     * 加载路由策略
     *
     * @return 路由实现
     */
    /**
     * 灰度 - 流量调拨比重 0-100
     */
    @Value("${czrpc.metas.grayRadio:10}")
    private int grayRadio;
    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(grayRadio);
    }

    /**
     * 加载消费端注册中心逻辑
     *
     * @return 注册中心实现
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRegistryCenter() {
        return new ZookeeperRegistryCenter();
    }

    /**
     * 加载缓存过滤器 - 与挡板互斥 选择其一注入
     *
     * @return 过滤器实现
     */
    @Bean
    public Filter cacheFilter() {
        return new CacheFilter();
    }

    /**
     * 加载挡板过滤器 - 与缓存互斥 选择其一注入
     *
     * @return 过滤器实现
     */
//    @Bean
//    public Filter mockFilter() {
//        return new MockFilter();
//    }

}
