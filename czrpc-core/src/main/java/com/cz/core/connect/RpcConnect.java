package com.cz.core.connect;

import okhttp3.MediaType;

/**
 * 扩展接口 - rpc 连接方式
 *
 * @author Zjianru
 */
public interface RpcConnect {

    MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    /**
     * okHttp / netty / httpclient / jdk urlConnection
     * RPC 通信
     *
     * @param rpcRequest rpc 请求数据
     * @return response
     */
    RpcResponse connect(RpcRequest rpcRequest);
}
