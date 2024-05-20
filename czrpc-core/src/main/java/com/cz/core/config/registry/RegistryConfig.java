package com.cz.core.config.registry;

import com.cz.core.registry.channel.Channel;
import com.cz.core.registry.channel.HttpChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * code desc
 *
 * @author Zjianru
 */

@Slf4j
@Configuration
public class RegistryConfig {
    @Bean
    public Channel channel() {
        return new HttpChannel(5000);
    }
}
