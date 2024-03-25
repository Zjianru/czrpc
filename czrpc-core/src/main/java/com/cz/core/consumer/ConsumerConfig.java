package com.cz.core.consumer;

import com.cz.core.cluster.RoundRobinLoadBalancer;
import com.cz.core.connect.LoadBalancer;
import com.cz.core.connect.Router;
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
    public LoadBalancer loadBalancer() {
        return new RoundRobinLoadBalancer();
    }

    /**
     * 加载路由策略
     *
     * @return 路由实现
     */
    @Bean
    public Router router() {
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
}
