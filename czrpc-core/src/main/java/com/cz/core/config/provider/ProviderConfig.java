package com.cz.core.config.provider;

import com.cz.core.config.AppProperties;
import com.cz.core.provider.ProviderBootstrap;
import com.cz.core.provider.ProviderInvoker;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.invoker.czRegistry.CzRegistryCenter;
import com.cz.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * provider 配置类
 *
 * @author Zjianru
 */
@Slf4j
@Configuration
@Import({ProviderProperties.class, AppProperties.class, SpringBootTransport.class})
public class ProviderConfig {


    @Value("${server.port:8080}")
    private String port;

    @Bean
    ProviderBootstrap providerBootstrap(@Autowired AppProperties appProperties,
                                        @Autowired ProviderProperties providerProperties) {
        return new ProviderBootstrap(port, appProperties, providerProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerInit(@Autowired ProviderBootstrap providerBootstrap) {
        return args -> providerBootstrap.start();
    }

    @Bean
    RegistryCenter providerRegistryCenter() {
//        return new ZookeeperRegistryCenter();
        return new CzRegistryCenter();
    }


}
