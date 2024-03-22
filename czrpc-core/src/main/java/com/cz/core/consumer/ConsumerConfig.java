package com.cz.core.consumer;

import com.cz.core.cluster.RoundRobinLoadBalancer;
import com.cz.core.connect.LoadBalancer;
import com.cz.core.connect.Router;
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

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerInit(@Autowired ConsumerBootstrap consumerBootstrap) {
        return args -> consumerBootstrap.start();
    }

    @Bean
    public LoadBalancer loadBalancer() {
//        return LoadBalancer.Default;
//        return new RandomLoadBalancer();
        return new RoundRobinLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }
}
