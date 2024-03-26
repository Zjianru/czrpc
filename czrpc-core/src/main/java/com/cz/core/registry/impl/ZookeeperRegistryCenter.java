package com.cz.core.registry.impl;

import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.Event;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.listener.ChangedListener;
import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * zookeeper 注册中心实现
 *
 * @author Zjianru
 */
public class ZookeeperRegistryCenter implements RegistryCenter {
    private CuratorFramework client;
    private TreeCache cache;

    /**
     * 环境信息
     */
    @Value("${czrpc.zkConfig.server}")
    private String zkServer;

    /**
     * 环境信息
     */
    @Value("${czrpc.zkConfig.root}")
    private String zkRoot;


    /**
     * 启动注册中心客户端
     */
    @Override
    public void start() {
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 链接 zookeeper
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServer)
                .namespace(zkRoot)
                .retryPolicy(retryPolicy)
                .build();
        System.out.println("zookeeper registry center start success! zkservice -->" + zkServer + " zkroot -->" + zkRoot);
        client.start();
    }

    /**
     * 停止注册中心客户端
     */
    @Override
    public void stop() {
        System.out.println("zookeeper registry center stop success!");
        cache.close();
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
    public void register(ServiceMeta service, InstanceMeta instance) {
        // 服务节点路径
        String servicePath = "/" + service.toPath();
        // 实例节点路径
        String instancePath = servicePath + "/" + instance.toPath();
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
    public void unRegister(ServiceMeta service, InstanceMeta instance) {
        // 服务节点路径
        String servicePath = "/" + service.toPath();
        // 实例节点路径
        String instancePath = servicePath + "/" + instance.toPath();
        try {
            // 检查服务节点路径
            if (client.checkExists().forPath(servicePath) == null) {
                // 没找到服务节点路径
                return;
            }
            // 删除实例节点
            System.out.println("zookeeper unRegistry center unregister success! DELETE PATH -->" + instancePath);
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
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        // 服务节点路径
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            System.out.println("zookeeper fetchAll success! service path -->" + servicePath);
            nodes.forEach(System.out::println);
            return mapInstance(nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<InstanceMeta> mapInstance(List<String> nodes) {
        return nodes.stream().map(x -> {
            String[] split = x.split("_");
            return InstanceMeta.http(split[0], Integer.parseInt(split[1]));
        }).collect(Collectors.toList());
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
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable().addListener((curator, event) -> {
            // 有任何节点变动 就会执行
            System.out.println("zk subscribe event:" + event);
            List<InstanceMeta> metas = fetchAll(service);
            listener.fire(new Event(metas));
        });
        cache.start();
    }
}
