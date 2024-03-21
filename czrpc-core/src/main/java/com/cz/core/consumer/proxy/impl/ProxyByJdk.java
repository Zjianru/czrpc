package com.cz.core.consumer.proxy.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.cz.core.connect.RpcConnect;
import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.connect.impl.OkHttpConnectImpl;
import com.cz.core.util.MethodUtils;
import com.cz.core.util.TypeUtils;

import java.lang.reflect.*;
import java.util.*;

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
            Object data = rpcResponse.getData();
            Class<?> type = method.getReturnType();
            if (data instanceof JSONObject jsonResult) {
                if (Map.class.isAssignableFrom(type)) {
                    Map resultMap = new HashMap<>();
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
//                        System.out.println("keyType  : " + keyType);
//                        System.out.println("valueType: " + valueType);
                        jsonResult.forEach((k, v) -> {
                            Object key = TypeUtils.cast(k, keyType);
                            Object value = TypeUtils.cast(v, valueType);
                            resultMap.put(key, value);
                        });
                    }
                    return resultMap;
                }
                return jsonResult.toJavaObject(type);
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object resultArray = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Array.set(resultArray, i, array[i]);
                    }
                    return resultArray;
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> resultList = new ArrayList<>(array.length);
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type actualType = parameterizedType.getActualTypeArguments()[0];
                        System.out.println(actualType);
                        for (Object o : array) {
                            resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
                        }
                    } else {
                        resultList.addAll(Arrays.asList(array));
                    }
                    return resultList;
                } else {
                    return null;
                }
            } else {
                return JSON.to(type, data);
            }
        }
        // 服务端异常信息传播到客户端
        Exception exception = rpcResponse.getException();
        throw new RuntimeException(exception);
    }


}
