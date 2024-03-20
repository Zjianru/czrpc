package com.cz.demo.consume;

import com.cz.core.annotation.czConsumer;
import com.cz.demo.api.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CzrpcDemoConsumeApplication {
    @czConsumer
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(CzrpcDemoConsumeApplication.class, args);
    }


}
