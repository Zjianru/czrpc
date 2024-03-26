package com.cz.core.consumer.proxy.invoker;

import com.cz.core.connect.RpcConnect;
import com.cz.core.connect.invoker.OkHttpInvoker;
import com.cz.core.context.RpcContext;
import com.cz.core.enhance.LoadBalancer;
import com.cz.core.enhance.Router;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
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
public class JdkProxyInvoker implements InvocationHandler {
    /**
     * 服务接口
     */
    Class<?> service;

    /**
     * rpc 上下文
     */
    RpcContext context;

    /**
     * 服务提供者信息
     */
    List<InstanceMeta> providerUrls;

    /**
     * RPC 连接器
     */
    RpcConnect rpcConnect = new OkHttpInvoker();

    public JdkProxyInvoker(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providerUrls) {
        this.service = service;
        this.context = rpcContext;
        this.providerUrls = providerUrls;
    }

    /**
     * Processes a method invocation on a proxy instance and returns
     * 动态代理已拦截请求，封装 RPC 请求并完成通信
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the Method instance corresponding to the interface method invoked on the proxy instance.
     * @param args   the arguments to the method
     * @return method return
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 封装 RPC 请求信息
        if (MethodUtils.isLocalMethod(method)) {
            return null;
        }
        String methodSign = MethodUtils.methodSign(method);
        RpcRequest request = new RpcRequest(service, method.getName(), methodSign, args, method.getParameterTypes());
        // 负载均衡处理
        Router<InstanceMeta> router = context.getRouter();
        LoadBalancer<InstanceMeta> loadBalancer = context.getLoadBalancer();
        InstanceMeta chosenProvider = LoadBalanceUtil.chooseProvider(router, loadBalancer, providerUrls);
        // 发起实际请求
        RpcResponse<?> rpcResponse = rpcConnect.connect(request, chosenProvider.transferToUrl());
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
