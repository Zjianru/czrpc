package com.cz.core.provider;

import com.cz.core.register.RegistryCenter;
import com.cz.core.register.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * provider 配置类
 *
 * @author Zjianru
 */
@Configuration
public class ProviderConfig {
    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerInit(@Autowired ProviderBootstrap providerBootstrap) {
        return args -> providerBootstrap.start();
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter providerRegistryCenter() {
        return new ZookeeperRegistryCenter();
    }


}
