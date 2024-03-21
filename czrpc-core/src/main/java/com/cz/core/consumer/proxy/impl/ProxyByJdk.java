package com.cz.core.consumer.proxy.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cz.core.connect.RpcConnect;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.connect.impl.OkHttpConnectImpl;
import com.cz.core.consumer.util.MethodUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 消费者代理类 - JDK 代理方式
 *
 * @author Zjianru
 */
public class ProxyByJdk implements InvocationHandler {
    Class<?> service;

    RpcConnect rpcConnect = new OkHttpConnectImpl();

    public ProxyByJdk(Class<?> service) {
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
        String methodSign = MethodUtils.methodSign(method);
        RpcRequest request = new RpcRequest(service, method.getName(), methodSign, args, method.getParameterTypes());
        RpcResponse rpcResponse = rpcConnect.connect(request);
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


}
