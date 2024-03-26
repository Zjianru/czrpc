package com.cz.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 服务实例元信息
 *
 * @author Zjianru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMeta {
    /**
     * 服务提供者的地址
     */
    private String host;

    /**
     * 服务提供者的端口
     */
    private Integer port;

    /**
     * 路径
     */
    private String context;

    /**
     * 协议头
     * eg: http / https
     */
    private String scheme;

    /**
     * online or offline
     */
    private boolean status;

    private Map<String, String> params;

    /**
     * 转换为注册中心的路径
     *
     * @return path
     */
    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    /**
     * 创建一个服务于 http 请求的实例
     *
     * @param host 服务提供者的地址
     * @param port 服务提供者的端口
     * @return InstanceMeta
     */
    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta(host, port, "", "http", true, null);
    }

    /**
     * 将信息映射转换为请求使用的 url
     *
     * @return url
     */
    public String transferToUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }
}
