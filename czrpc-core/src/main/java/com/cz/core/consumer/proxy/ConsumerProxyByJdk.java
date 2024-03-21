package com.cz.core.consumer.proxy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.consumer.util.MethodUtils;
import okhttp3.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
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

    public ConsumerProxyByJdk(Class<?> service) {
        this.service = service;
    }

    /**
     * 动态代理已拦截请求，封装 RPC 请求并完成通信
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (MethodUtils.isLocalMethod(method.getName())) {
            return null;
        }
        RpcRequest request = new RpcRequest(service, method.getName(), args, method.getParameterTypes());
        RpcResponse rpcResponse = RpcConnectByOkHttp(request);
        if (rpcResponse == null) {
            return new RuntimeException(
                    String.format("Invoke class [%s] method [%s(%s)] error, params:[%S]",
                            service, method.getName(), Arrays.toString(method.getParameterTypes()), Arrays.toString(args)
                    ));
        }
        if (rpcResponse.isStatus()) {
            if (rpcResponse.getData() instanceof JSONObject jsonResult) {
                return jsonResult.toJavaObject(method.getReturnType());
            } else {
                return JSON.to(method.getReturnType(), rpcResponse.getData());
            }
        }
        // 服务端异常信息传播到客户端
        Exception exception = rpcResponse.getException();
        throw new RuntimeException(exception);
    }

    /**
     * okHttp / netty / httpclient / jdk urlConnection
     * RPC 通信
     *
     * @param rpcRequest rpc 请求数据
     * @return rpc 相应
     */
    private RpcResponse RpcConnectByOkHttp(RpcRequest rpcRequest) {
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                    .readTimeout(1, TimeUnit.SECONDS)
                    .writeTimeout(1, TimeUnit.SECONDS)
                    .connectTimeout(1, TimeUnit.SECONDS).build();
            String requestJson = JSON.toJSONString(rpcRequest);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/")
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
