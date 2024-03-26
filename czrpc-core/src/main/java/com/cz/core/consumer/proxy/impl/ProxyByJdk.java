package com.cz.core.consumer.proxy.impl;

import com.cz.core.connect.*;
import com.cz.core.connect.impl.OkHttpConnectImpl;
import com.cz.core.context.RpcContext;
import com.cz.core.util.LoadBalanceUtil;
import com.cz.core.util.MethodUtils;
import com.cz.core.util.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 消费者代理类 - JDK 代理方式
 *
 * @author Zjianru
 */
public class ProxyByJdk implements InvocationHandler {
    Class<?> service;
    RpcContext context;
    List<String> providerUrls;
    RpcConnect rpcConnect = new OkHttpConnectImpl();

    public ProxyByJdk(Class<?> service, RpcContext rpcContext, List<String> providerUrls) {
        this.service = service;
        this.context = rpcContext;
        this.providerUrls = providerUrls;
    }

    /**
     * 动态代理已拦截请求，封装 RPC 请求并完成通信
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 封装 RPC 请求信息
        if (MethodUtils.isLocalMethod(method.getName())) {
            return null;
        }
        String methodSign = MethodUtils.methodSign(method);
        RpcRequest request = new RpcRequest(service, method.getName(), methodSign, args, method.getParameterTypes());

        // 负载均衡处理
        Router router = context.getRouter();
        LoadBalancer loadBalancer = context.getLoadBalancer();
        String chosenProvider = LoadBalanceUtil.chooseProvider(router, loadBalancer, providerUrls);

        // 发起实际请求
        RpcResponse rpcResponse = rpcConnect.connect(request, chosenProvider);

        // 返回值处理
        if (rpcResponse == null) {
            return new RuntimeException(
                    String.format("Invoke class [%s] method [%s(%s)] error, params:[%S]",
                            service, method.getName(), Arrays.toString(method.getParameterTypes()), Arrays.toString(args)
                    ));
        }
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        }
        // 服务端异常信息传播到客户端
        Exception exception = rpcResponse.getException();
        throw new RuntimeException(exception);
    }
}
