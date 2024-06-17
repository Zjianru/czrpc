czrpc 是一个向 dubbo看齐，并努力超越 dubbo 的 RPC 框架

默认使用 zookeeper 作为注册中心

当前已实现功能:
- 路由
    - 标签路由
    - 灰度路由
- 过滤器
- 可插拔通信组件,可选 HTTP 或 Netty
- 负载均衡
    - RR
    - 带权 RR
    - hash
- 优雅启停
- namespace 隔离
- 限流
  - 不同服务层级限流
  - 数据库访问限流
  - 线程池与调用计数
- 容错机制
  - 时间窗口计数
  - 故障隔离
  - 故障探测与恢复
- 重试与超时
  - 不同层级的超时控制
  - 重试策略和回退策略
  - 超时漏斗设计
- 挡板
- 多机房容灾
  - 基于Tag实现DC/zone 路由支持
  - 基于DC/zone 实现流量调拨
  - 实现多DC/zone 的服务调用和LB
- 实现灰度发布
  - 支持不同层级服务灰度
  - 支持全链路灰度发布

后续追加能力: 

- [x] 集成自研配置中心
- [x] 集成自研注册中心
- [ ] 集成自研元数据中心

## 集成 czRegistry 注册中心

czRegistry 注册中心目前已提供入口并完成适配
顶层接口为`RegistryCenter`,具体实现为`CzRegistryCenter`
可通过调整配置文件配置使用

## 集成 czConfig 配置中心

czConfig 配置中心目前已完成对接,可配置使用

czrpc-core 中 pom 已添加如下依赖:

```xml
        <!-- 集成 czConfig 需要下面两个依赖-->
<dependency>
  <groupId>com.cz</groupId>
  <artifactId>czConfig-client</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-context</artifactId>
<version>4.1.2</version>
</dependency>
```

使用时 需要在使用 configuration 处 添加 `@EnableCzConfig`注解来启用

配置使用与 spring 相同 可使用 spring 的 `@value` 注解 或 `@ConfigurationProperties` 注解来获取配置
配置中心中数据发生时 将会有最晚 3s 的延迟, 将新的配置数据刷入