package com.cz.demo.provider;

import com.cz.test.TestZkServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProviderApplicationTests {


    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> czrpcDemoProviderApplicationTests  .... ");
    }

    @AfterAll
    static void destory() {
        zkServer.stop();
    }

}
