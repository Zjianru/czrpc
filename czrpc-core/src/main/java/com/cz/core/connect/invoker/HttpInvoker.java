package com.cz.core.connect.invoker;

import com.alibaba.fastjson2.JSON;
import com.cz.core.connect.RpcConnect;
import com.cz.core.ex.ExErrorCodes;
import com.cz.core.ex.RpcException;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 请求目标服务
 *
 * @author Zjianru
 */
@Service
public class HttpInvoker implements RpcConnect {
    private final OkHttpClient client;

    MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public HttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MICROSECONDS)
                .writeTimeout(timeout, TimeUnit.MICROSECONDS)
                .connectTimeout(timeout, TimeUnit.MICROSECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * connect provider and send meta data
     *
     * @param rpcRequest  request info
     * @param providerUrl 负载均衡后得到的提供者请求路径
     * @return response
     */
    @Override
    public RpcResponse<?> connect(RpcRequest rpcRequest, String providerUrl) {
        try {
            String requestJson = JSON.toJSONString(rpcRequest);
            Request request = new Request.Builder()
                    .url(providerUrl)
                    .post(RequestBody.create(requestJson, JSON_TYPE))
                    .build();
            String response = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            return JSON.parseObject(response, RpcResponse.class);
        } catch (Exception e) {
            throw new RpcException(e, ExErrorCodes.SOCKET_TIME_OUT);
        }
    }
}
