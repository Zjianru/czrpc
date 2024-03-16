package com.cz.demo.provider.user.controller;

import com.cz.czrpc.core.connect.RpcRequest;
import com.cz.czrpc.core.connect.RpcResponse;
import com.cz.demo.provider.scan.AnnotationScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RestController
public class UserController {
    @Autowired
    AnnotationScan annotationScan;

    // http+json 实现序列化和通信
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return invokeRequest(request);
    }

    private RpcResponse invokeRequest(RpcRequest request) {
        Object bean = annotationScan.skeleton.get(request.getService().getCanonicalName());
        try {
            Method method = bean.getClass().getMethod(request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse<>(true, result);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
