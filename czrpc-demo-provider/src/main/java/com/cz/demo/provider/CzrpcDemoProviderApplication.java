package com.cz.demo.provider;

import com.cz.core.annotation.czProvider;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.provider.ProviderBootstrap;
import com.cz.demo.api.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@Import(ProviderBootstrap.class)
public class CzrpcDemoProviderApplication {

    @Autowired
    ProviderBootstrap providerBootstrap;

    public static void main(String[] args) {
        SpringApplication.run(CzrpcDemoProviderApplication.class, args);
    }


    // http+json 实现序列化和通信
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }


    @Bean
    ApplicationRunner privateRunner() {
        return args -> {
            System.out.println("Provider started");
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setService(UserService.class);
            rpcRequest.setMethod("findById");
            rpcRequest.setArgs(new Object[]{100});
            RpcResponse invoke = invoke(rpcRequest);
            System.out.println(invoke.getData());
        };
    }
}
