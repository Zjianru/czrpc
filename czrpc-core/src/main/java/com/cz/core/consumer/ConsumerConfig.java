package com.cz.core.consumer;

import com.cz.core.enhance.Router;
import com.cz.core.filter.Filter;
import com.cz.core.filter.policy.CacheFilter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.loadBalance.policy.RoundRobinLoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.impl.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Bean
    public Router<InstanceMeta> router() {
        return Router.Default;
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
     * 加载过滤器
     *
     * @return 过滤器实现
     */
    @Bean
    public Filter filter() {
        return new CacheFilter();
    }

}
