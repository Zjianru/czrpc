package com.cz.core.registry.impl;

import com.cz.core.registry.Event;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.listener.ChangedListener;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * zookeeper 注册中心实现
 *
 * @author Zjianru
 */
public class ZookeeperRegistryCenter implements RegistryCenter {
    private CuratorFramework client;

    /**
     * 启动注册中心客户端
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
        System.out.println("zookeeper registry center start success!");
        client.start();
    }

    /**
     * 停止注册中心客户端
     */
    @Override
    public void stop() {
        System.out.println("zookeeper registry center stop success!");
        client.close();
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
            System.out.println("zookeeper registry center register success! CREATE PATH -->" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            System.out.println("zookeeper registry center unregister success! DELETE PATH -->" + instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取全部消费者信息
     *
     * @param service service info
     * @return provide instance info in service
     */
    @Override
    public List<String> fetchAll(String service) {
        // 服务节点路径
        String servicePath = "/" + service;
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println("zookeeper fetchAll success! service path -->" + servicePath);
            nodes.forEach(System.out::println);
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 消费端订阅
     * 感知注册中心数据变化
     *
     * @param service  service info
     * @param listener listener
     */
    @Override
    @SneakyThrows
    public void subscribe(String service, ChangedListener listener) {
        CuratorCache cache = CuratorCache.build(client, "/" + service);
        cache.listenable().addListener((type, childData, childData1) -> {
            List<String> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
        cache.start();
    }
}
