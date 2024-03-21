package com.cz.demo.consumer;

import com.cz.core.annotation.czConsumer;
import com.cz.core.consumer.ConsumerConfig;
import com.cz.demo.api.service.OrderService;
import com.cz.demo.api.service.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerConfig.class)
public class CzrpcDemoConsumeApplication {
    @czConsumer
    UserService userService;
    @czConsumer
    OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(CzrpcDemoConsumeApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumerRunner() {
        return args -> {
            System.out.println("user test 111 -->" + userService.findById(100));
            System.out.println("user test 222 -->" + userService.findById(100, "test"));
        };
    }
}
