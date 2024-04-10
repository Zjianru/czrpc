package com.cz.demo.consumer;

import com.cz.demo.api.pojo.User;
import com.cz.demo.api.service.UserService;
import com.cz.demo.provider.ProviderApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootTest(classes = ConsumerApplication.class)
class ConsumeTests {

    static ApplicationContext context;

    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");

        zkServer.start();
        context = SpringApplication.run(ProviderApplication.class,
                "--server.port=8094",
                "--czrpc.zkServer=localhost:2182",
                "--czrpc.root=czrpc",
                "--logging.level.com.cz=info");
    }

    @Test
    void contextLoads() {
        log.info(" ===> consumer testing  .... ");
        UserService bean = context.getBean(UserService.class);
        User user = bean.find(1000);
        log.info("test finished result is {}", user);
    }

    @AfterAll
    static void destory() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }


}
