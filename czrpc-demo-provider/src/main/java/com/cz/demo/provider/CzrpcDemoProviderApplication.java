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
public class CzrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CzrpcDemoProviderApplication.class, args);
    }

}
