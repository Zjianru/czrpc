package com.cz.demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * provider application
 *
 * @author Zjianru
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.cz.demo.provider", "com.cz.core.provider"})
public class CzrpcDemoProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(CzrpcDemoProviderApplication.class, args);
    }

}

