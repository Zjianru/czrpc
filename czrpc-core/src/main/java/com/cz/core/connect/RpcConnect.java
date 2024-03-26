package com.cz.core.connect;

import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;

/**
 * 扩展接口 - rpc 连接方式
 *
 * @author Zjianru
 */
public interface RpcConnect {


    /**
     * okHttp / netty / httpclient / jdk urlConnection
     * RPC 通信
     *
     * @param rpcRequest  rpc 请求数据
     * @param providerUrl 负载均衡后得到的提供者请求路径
     * @return response
     */
    RpcResponse<?> connect(RpcRequest rpcRequest, String providerUrl);
}
