package com.cz.demo.consumer;

import lombok.SneakyThrows;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

/**
 * consumer zookeeper server for test
 *
 * @author Zjianru
 */
public class TestZkServer {

    TestingServer server = null;

    @SneakyThrows
    public void start() {
        server = new TestingServer(2182);
        server.start();
        System.out.println("TestingZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        server.stop();
        CloseableUtils.closeQuietly(server);
        System.out.println("TestingZooKeeperServer stopped.");
    }
}
