package com.cz.core.config.consumer;

import com.cz.core.config.AppProperties;
import com.cz.core.consumer.ConsumerBootstrap;
import com.cz.core.context.RpcContext;
import com.cz.core.filter.Filter;
import com.cz.core.filter.policy.RequestParamFilter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.loadBalance.policy.RoundRobinLoadBalancer;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.invoker.ZookeeperRegistryCenter;
import com.cz.core.router.Router;
import com.cz.core.router.policy.GrayRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * consumer 配置类
 * Config for consumer.
 *
 * @author Zjianru
 */
@Slf4j
@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    AppProperties appProperties;

    @Autowired
    ConsumerProperties consumerProperties;

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
    @Order(Integer.MIN_VALUE + 1)
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
     * @return router
     */
    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

    /**
     * 加载消费端注册中心逻辑
     *
     * @return 注册中心实现
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRegistryCenter() {
        return new ZookeeperRegistryCenter();
    }

    /**
     * 加载跨线程传递信息过滤器
     *
     * @return 过滤器实现
     */
    @Bean
    public Filter RequestParamFilter() {
        return new RequestParamFilter();
    }


    /**
     * wrapper context 信息
     *
     * @return rpcContext
     */
    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext ctx = RpcContext.builder()
                .filters(filters)
                .loadBalancer(loadBalancer)
                .router(router)
                .build();
        ctx.setParams(wrapperBaseParams(appProperties));
        ctx.setConsumerProperties(consumerProperties);
        return ctx;
    }

    /**
     * 封装 rpcContext 基本信息
     *
     * @param appProperties 应用级别的配置
     * @return rpcContext
     */
    private Map<String, String> wrapperBaseParams(AppProperties appProperties) {
        Map<String, String> params = new HashMap<>();
        params.put("applicationId", appProperties.getApplicationId());
        params.put("env", appProperties.getEnv());
        params.put("nameSpace", appProperties.getNameSpace());
        return params;
    }
}
