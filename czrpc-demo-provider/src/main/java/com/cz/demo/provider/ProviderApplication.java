package com.cz.demo.provider;

import com.cz.core.provider.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * provider application
 *
 * @author Zjianru
 */
@SpringBootApplication
@Import({ProviderConfig.class})
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}

