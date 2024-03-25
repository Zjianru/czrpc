package com.cz.core.provider;

import com.cz.core.register.RegistryCenter;
import com.cz.core.register.ZookeeperRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * provider 配置类
 *
 * @author Zjianru
 */
@Configuration
public class ProviderConfig {
    @Bean
    ProviderBootstrap createProvider() {
        return new ProviderBootstrap();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter providerRegistryCenter() {
        return new ZookeeperRegistryCenter();
    }


}
