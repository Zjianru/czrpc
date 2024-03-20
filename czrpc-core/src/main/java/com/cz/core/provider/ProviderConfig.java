package com.cz.core.provider;

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
    ProviderBootstrap providerBootstrap(){
        return new ProviderBootstrap();
    }
}
