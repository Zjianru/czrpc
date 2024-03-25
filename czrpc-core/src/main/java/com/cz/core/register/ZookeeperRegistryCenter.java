package com.cz.core.register;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * code desc
 *
 * @author Zjianru
 */
public class ZookeeperRegistryCenter implements RegistryCenter {
    private CuratorFramework client;

    /**
     *
     */
    @Override
    public void start() {
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 链接 zookeeper
        client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .namespace("czrpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        System.out.println("zookeeper registry center start success!%n");
    }

    /**
     *
     */
    @Override
    public void stop() {
        client.close();
        System.out.println("zookeeper registry center stop success!%n");
    }

    /**
     * 注册上线
     * 服务与实例
     *
     * @param service  服务 - 持久化节点
     * @param instance 实例 - 临时节点
     */
    @Override
    public void register(String service, String instance) {
        // 服务节点路径
        String servicePath = "/" + service;
        // 实例节点路径
        String instancePath = servicePath + "/" + instance;
        try {
            if (client.checkExists().forPath(servicePath) == null) {
                // 没找到服务节点路径，创建持久化节点
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例节点路径
            System.out.println("zookeeper registry center register success!%n CREATE PATH -->" + instancePath + "%n");
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册下线
     * 服务与实例
     *
     * @param service  服务 - 持久化节点
     * @param instance 实例 - 临时节点
     */
    @Override
    public void unRegister(String service, String instance) {
        // 服务节点路径
        String servicePath = "/" + service;
        // 实例节点路径
        String instancePath = servicePath + "/" + instance;
        try {
            // 检查服务节点路径
            if (client.checkExists().forPath(servicePath) == null) {
                // 没找到服务节点路径
                return;
            }
            // 删除实例节点
            System.out.println("zookeeper registry center unregister success!%n DELETE PATH -->" + instancePath + "%n");
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param service
     * @return
     */
    @Override
    public List<String> fetchAll(String service) {
        return null;
    }

    /**
     * @param service
     */
    @Override
    public void subscribe(String service) {

    }
}
