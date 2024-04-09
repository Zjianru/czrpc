package com.cz.core.registry.impl;

import com.alibaba.fastjson2.JSON;
import com.cz.core.ex.ExErrorCodes;
import com.cz.core.ex.RpcException;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.meta.ServiceMeta;
import com.cz.core.registry.Event;
import com.cz.core.registry.RegistryCenter;
import com.cz.core.registry.listener.ChangedListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * zookeeper 注册中心实现
 *
 * @author Zjianru
 */
@Slf4j
public class ZookeeperRegistryCenter implements RegistryCenter {

    /**
     * 环境信息
     */
    @Value("${czrpc.zkConfig.server:localhost:2181}")
    private String zkServer;

    /**
     * 环境信息
     */
    @Value("${czrpc.zkConfig.root:czrpc}")
    private String zkRoot;

    /**
     * zookeeper client
     */
    private CuratorFramework client = null;

    /**
     * zookeeper caches
     */
    private final List<TreeCache> caches = new ArrayList<>();

    /**
     * check param
     */
    private final boolean running = false;

    /**
     * 启动注册中心客户端
     */
    @Override
    public void start() {
        if (running) {
            log.info(" ===> zk client has started to server[{}/{}], ignored.", zkServer, zkRoot);
            return;
        }
        // 重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 链接 zookeeper
        client = CuratorFrameworkFactory.builder()
                .connectString(zkServer)
                .namespace(zkRoot)
                .retryPolicy(retryPolicy)
                .build();
        log.info("zookeeper registry center start success! zkservice -->{} zkroot -->{}", zkServer, zkRoot);
        client.start();
    }

    /**
     * 停止注册中心客户端
     */
    @Override
    public void stop() {
        if (!running) {
            log.info(" ===> zk client isn't running to server[{}/{}], ignored.", zkServer, zkRoot);
            return;
        }
        log.info("zookeeper registry center stop success!");
        caches.forEach(TreeCache::close);
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
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.metasTransfer().getBytes());
            }
            // 创建实例节点路径
            log.info("zookeeper registry center register success! CREATE PATH -->{}", instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.metasTransfer().getBytes());
        } catch (Exception e) {
            throw new RpcException(e, ExErrorCodes.REGISTER_CENTER_ERROR);
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
            log.info("zookeeper unRegistry center unregister success! DELETE PATH -->{}", instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RpcException(e, ExErrorCodes.REGISTER_CENTER_ERROR);
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
            log.info("zookeeper fetchAll success! service path -->{}", servicePath);
            return mapInstance(nodes, servicePath);
        } catch (Exception e) {
            throw new RpcException(e, ExErrorCodes.REGISTER_CENTER_ERROR);
        }
    }

    /**
     * 组装 instance 信息
     *
     * @param nodes       节点
     * @param servicePath 服务路径
     * @return 组装好的 instance
     */
    private List<InstanceMeta> mapInstance(List<String> nodes, String servicePath) {
        return nodes.stream().map(x -> {
            String[] split = x.split("_");
            InstanceMeta instance = InstanceMeta.http(split[0], Integer.parseInt(split[1]));
            log.debug("instance url ==>{}", instance.transferToUrl());
            System.out.println("instance url ==> " + instance.transferToUrl());
            byte[] bytes;
            try {
                bytes = client.getData().forPath(servicePath + "/" + x);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String, Object> params = JSON.parseObject(new String(bytes));
            params.forEach((k, v) -> {
                log.debug("{} -> {}", k, v);
                System.out.println("k--> " + k + " v-->" + v);
                Map<String, String> instanceParams = instance.getParams();
                if (instanceParams == null) {
                    instanceParams = new HashMap<>();
                }
                instanceParams.put(k, v == null ? null : v.toString());
                instance.setParams(instanceParams);
            });
            log.debug("instance metasTransfer ==>{}", instance.metasTransfer());
            System.out.println("instance metasTransfer ==>{" + instance.metasTransfer() + "}");
            return instance;
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
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable().addListener((curator, event) -> {
            synchronized (ZookeeperRegistryCenter.class) {
//                if (running) {
                // 有任何节点变动 就会执行
                log.info("zk subscribe event:{}", event);
                List<InstanceMeta> metas = fetchAll(service);
                listener.fire(new Event(metas));
//                }
            }
        });
        cache.start();
        caches.add(cache);
    }
}
