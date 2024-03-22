package com.cz.core.connect.impl;

import com.alibaba.fastjson2.JSON;
import com.cz.core.connect.RpcConnect;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * code desc
 *
 * @author Zjianru
 */
@Service
public class OkHttpConnectImpl implements RpcConnect {

    /**
     * connect provider and send meta data
     *
     * @param rpcRequest request info
     * @param providerUrl 负载均衡后得到的提供者请求路径
     * @return response
     */
    @Override
    public RpcResponse connect(RpcRequest rpcRequest, String providerUrl) {
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                    .readTimeout(1, TimeUnit.SECONDS)
                    .writeTimeout(1, TimeUnit.SECONDS)
                    .connectTimeout(1, TimeUnit.SECONDS).build();
            String requestJson = JSON.toJSONString(rpcRequest);
            Request request = new Request.Builder()
                    .url(providerUrl)
                    .post(RequestBody.create(requestJson, JSON_TYPE))
                    .build();
            String response = Objects.requireNonNull(httpClient.newCall(request).execute().body()).string();
            RpcResponse rpcResponse = JSON.parseObject(response, RpcResponse.class);
            return rpcResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
