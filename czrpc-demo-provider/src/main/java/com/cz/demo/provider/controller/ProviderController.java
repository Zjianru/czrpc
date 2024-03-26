package com.cz.demo.provider.controller;

import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.provider.ProviderBootstrap;
import com.cz.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 请求转化为 RPC 请求
 * 完成接口测试
 *
 * @author Zjianru
 */
@RestController
@Import(ProviderBootstrap.class)
public class ProviderController {

    @Autowired
    ProviderInvoker providerInvoker;

    /**
     * http+json 实现序列化和通信
     *
     * @param request 指定的接口方法和参数
     * @return 接口返回值
     */
    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    /**
     * http+json 实现序列化和通信
     *
     * @param request 指定的接口方法和参数
     * @return 接口返回值
     */
    @RequestMapping("/endPoint2")
    public RpcResponse<Object> invoke2(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }
}
