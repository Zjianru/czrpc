package com.cz.demo.consumer;

import com.cz.demo.provider.ProviderApplication;
import com.cz.test.TestZkServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

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
        System.out.println(" ===> aaaa  .... ");
    }

    @AfterAll
    static void destory() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }


}
