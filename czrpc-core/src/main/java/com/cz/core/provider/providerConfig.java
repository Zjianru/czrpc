package com.cz.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * code desc
 *
 * @author Zjianru
 */
@Configuration
public class providerConfig {
    @Bean
    ProviderBootstrap providerBootstrap(){
        return new ProviderBootstrap();
    }
}
