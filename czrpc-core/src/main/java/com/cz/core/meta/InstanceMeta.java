package com.cz.core.meta;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
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
     * 蓝绿 - 上线状态
     */
    private boolean status;

    /**
     * 附加参数
     * 目前包含
     * [打标]-> gray/unit/dc
     * [蓝绿]-> online
     */
    private Map<String, String> params;

    /**
     * 设置附加参数列表
     *
     * @param params 附加参数
     * @return instanceMeta
     */
    public InstanceMeta addParams(Map<String, String> params) {
        Map<String, String> peek = this.getParams();
        if (peek == null) {
            peek = new HashMap<>();
        }
        peek.putAll(params);
        this.setParams(peek);
        return this;
    }

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
        return new InstanceMeta(host, port, "czrpc", "http", true, null);
    }

    /**
     * 将信息映射转换为请求使用的 url
     *
     * @return url
     */
    public String transferToUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    /**
     * 将附加参数列表转换为 json 串
     *
     * @return json string
     */
    public String metasTransfer() {
        return JSON.toJSONString(getParams());
    }

    /**
     * 将附加参数列表转换为 json 串
     *
     * @return json string
     */
    public String dataToJson() {
        return JSON.toJSONString(this);
    }
}
