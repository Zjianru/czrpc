package com.cz.core.consumer.proxy.invoker;

import com.cz.core.connect.RpcConnect;
import com.cz.core.connect.invoker.OkHttpInvoker;
import com.cz.core.context.RpcContext;
import com.cz.core.ex.ExErrorCodes;
import com.cz.core.ex.RpcException;
import com.cz.core.filter.Filter;
import com.cz.core.governance.SlidingTimeWindow;
import com.cz.core.meta.InstanceMeta;
import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.util.LoadBalanceUtil;
import com.cz.core.util.MethodUtils;
import com.cz.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费者代理类 - JDK 代理方式
 *
 * @author Zjianru
 */
@Slf4j
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
     * RPC 连接器
     */
    RpcConnect rpcConnect;

    /**
     * 服务提供者信息
     */
    final List<InstanceMeta> providerUrls;

    /**
     * 被隔离的提供者信息
     */
    final List<InstanceMeta> isolatedInstance = new LinkedList<>();

    /**
     * 探活的提供者信息
     */
    final List<InstanceMeta> halfOpenInstance = new LinkedList<>();

    /**
     * 记录提供者异常次数的滑动时间集合
     */
    final Map<String, SlidingTimeWindow> windows = new HashMap<>();

    /**
     * 探活线程
     */
    ScheduledExecutorService executor;

    /**
     * constructor
     *
     * @param service      service
     * @param rpcContext   rpcContext
     * @param providerUrls providerUrls
     */
    public JdkProxyInvoker(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providerUrls) {
        this.service = service;
        this.context = rpcContext;
        this.providerUrls = providerUrls;
        int timeout = Integer.parseInt(context.getParams().getOrDefault("retries.retryTimeout", "1000"));
        this.rpcConnect = new OkHttpInvoker(timeout);
        isolateAndHalfOpenConfig(
                Long.parseLong(context.getParams().getOrDefault("isolate.halfOpen.initialDelay", "10000")),
                Long.parseLong(context.getParams().getOrDefault("isolate.halfOpen.delay", "60000"))
        );
    }


    /**
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
        RpcRequest request = RpcRequest.builder()
                .service(service)
                .methodName(method.getName())
                .methodSign(MethodUtils.methodSign(method))
                .args(args)
                .argsType(method.getParameterTypes())
                .build();
        List<Filter> filters = context.getFilters();
        int retries = context.getRetries();
        int faultLimit = Integer.parseInt(context.getParams().getOrDefault("czrpc.isolate.faultLimit", "10"));

        while (retries-- > 0) {
            log.info("Invoke class [{}] method [{}] params: [{}], retry times: [{}]",
                    service, method.getName(), Arrays.toString(args), retries);
            try {
                // 前置过滤器处理
                // TODO cacheFilter 得放在所有后置 filter 的最后一个，不然有可能缓存的是上一次中间阶段的 rpcResponse
                for (Filter filter : filters) {
                    Object perFilterResponse = filter.perProcess(request);
                    if (perFilterResponse != null) {
                        log.debug("{}==>perProcess return: {}", filter.getClass().getName(), perFilterResponse);
                        return perFilterResponse;
                    }
                }

                // 半开探活与负载均衡
                InstanceMeta chosenProvider;
                synchronized (halfOpenInstance) {
                    if (halfOpenInstance.isEmpty()) {
                        // 不需要进行探活，调动负载均衡进行处理
                        log.debug("==>不需要进行探活<==");
                        chosenProvider = LoadBalanceUtil.chooseProvider(context.getRouter(), context.getLoadBalancer(), providerUrls);
                        log.debug("finally load balance choose is ===> {}", chosenProvider);
                    } else {
                        log.debug("==>正在进行探活<==");
                        // 随便取出一个节点进行探活，由于探活频次，对流量影响可忽略不计
                        chosenProvider = halfOpenInstance.remove(0);
                    }
                }

                String chosenProviderUrl = chosenProvider.transferToUrl();
                RpcResponse<?> rpcResponse;
                Object result;
                // 故障隔离
                try {
                    // 发起实际请求
                    rpcResponse = rpcConnect.connect(request, chosenProviderUrl);
                    // 返回值处理
                    result = responseCastToResult(method, args, rpcResponse);
                } catch (Exception e) {
                    // 故障的规则统计与隔离
                    // 不加入并发限制，可导致异常超过 10 次仍不被隔离，并非严格遵循配置次数
                    synchronized (windows) {
                        SlidingTimeWindow window = windows.computeIfAbsent(chosenProviderUrl, k -> new SlidingTimeWindow());
                        // 每一次异常，记录一次，统计 30S 之内的异常次数
                        window.record(System.currentTimeMillis());
                        int exceptionTimes = window.getSum();
                        log.debug("provider {} in window with {}", chosenProviderUrl, exceptionTimes);
                        // 单位时间内异常次数超限，进行故障隔离
                        if (exceptionTimes >= faultLimit) {
                            isolate(chosenProvider);
                        }
                    }
                    throw e;
                }

                // 后置过滤器处理
                for (Filter filter : filters) {
                    Object postFilterResponse = filter.postProcess(request, rpcResponse, result);
                    if (postFilterResponse != null) {
                        log.debug("{}==>postProcess return: {}", filter.getClass().getName(), postFilterResponse);
                        return postFilterResponse;
                    }
                }

                // 放开被隔离的提供者节点
                synchronized (providerUrls) {
                    if (!providerUrls.contains(chosenProvider)) {
                        isolatedInstance.remove(chosenProvider);
                        providerUrls.add(chosenProvider);
                        log.debug("已放开被隔离的提供者节点==>{}，\n 当前 providerUrls==>{},\n 当前 isolatedInstance==>{}",
                                chosenProvider, providerUrls, isolatedInstance);
                    }
                }

                return result;
            } catch (RpcException e) {
                log.error("Invoke class [{}] method [{}] error, params:[{}], retry times: [{}]",
                        service, method.getName(), Arrays.toString(args), retries);
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * 配置半开探活的线程池
     * 默认探活频次间隔 - 毫秒
     *
     * @param initialDelay 初始延迟
     * @param delay        频次间隔
     */
    private void isolateAndHalfOpenConfig(long initialDelay, long delay) {
        this.executor = Executors.newScheduledThreadPool(1);
        // 在给定的初始延迟之后，按照固定的延迟时间周期性地执行任务
        this.executor.scheduleWithFixedDelay(this::halfOpen, initialDelay, delay, TimeUnit.MICROSECONDS);
    }

    /**
     * 半开探活
     */
    private void halfOpen() {
        log.debug("===> halfOpenInstance ==> {},isolatedInstance==>{}", halfOpenInstance, isolatedInstance);
        halfOpenInstance.clear();
        halfOpenInstance.addAll(isolatedInstance);
    }

    /**
     * 完成故障隔离
     *
     * @param chosenProvider 待隔离的提供者
     */
    private void isolate(InstanceMeta chosenProvider) {
        log.debug("current isolate provider {}", chosenProvider);
        providerUrls.remove(chosenProvider);
        log.debug("finished isolate ... current providers ==> {}", providerUrls);
        isolatedInstance.add(chosenProvider);
        log.debug("finished isolate... current isolatedUrls is ==> {}", isolatedInstance);
    }

    /**
     * 返回值处理
     *
     * @param method      method
     * @param args        args
     * @param rpcResponse rpcResponse
     * @return Object
     */
    private Object responseCastToResult(Method method, Object[] args, RpcResponse<?> rpcResponse) {
        if (rpcResponse == null) {
            return new RpcException(
                    String.format("Invoke class [%s] method [%s(%s)] error, params:[%S]",
                            service, method.getName(), Arrays.toString(method.getParameterTypes()), Arrays.toString(args),
                            ExErrorCodes.INVOKER_ERROR));
        }
        if (rpcResponse.isStatus()) {
            return TypeUtils.castMethodResult(method, rpcResponse.getData());
        } else {
            // 服务端异常信息传播到客户端
            Exception exception = rpcResponse.getException();
            if (exception instanceof RpcException rpcException) {
                throw rpcException;
            } else {
                throw new RpcException(exception, ExErrorCodes.UNKNOWN_ERROR);
            }
        }
    }
}
