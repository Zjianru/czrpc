package com.cz.core.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消费者代理类 - JDK 代理方式
 *
 * @author Zjianru
 */
public class ConsumerProxyByJdk implements InvocationHandler {
    Class<?> service;

    final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS).build();

    public ConsumerProxyByJdk(Class<?> service) {
        this.service = service;
    }

    /**
     * 动态代理已拦截请求，封装 RPC 请求并完成通信
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setService(service);
        request.setMethod(method.getName());
        request.setArgs(args);
        RpcResponse rpcResponse = RpcConnect(request);
        if (rpcResponse.isStatus()) {
            JSONObject jsonResponse = (JSONObject) rpcResponse.getData();
            Object javaObject = jsonResponse.toJavaObject(method.getReturnType());
            return javaObject;
        }
        return null;
    }

    /**
     * okHttp / netty / httpclient / jdk urlConnection
     * RPC 通信
     *
     * @param rpcRequest rpc 请求数据
     * @return rpc 相应
     */
    private RpcResponse RpcConnect(RpcRequest rpcRequest) {
        try {
            String requestJson = JSON.toJSONString(rpcRequest);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/")
                    .post(RequestBody.create(requestJson, JSON_TYPE))
                    .build();
            String response = Objects.requireNonNull(httpClient.newCall(request).execute().body()).string();
            RpcResponse rpcResponse = JSON.parseObject(response, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
