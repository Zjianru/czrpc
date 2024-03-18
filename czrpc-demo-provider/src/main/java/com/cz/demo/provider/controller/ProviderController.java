package com.cz.demo.provider.controller;

import com.cz.core.connect.RpcRequest;
import com.cz.core.connect.RpcResponse;
import com.cz.core.provider.ProviderBootstrap;
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
    ProviderBootstrap providerBootstrap;

    /**
     * http+json 实现序列化和通信
     *
     * @param request 指定的接口方法和参数
     * @return 接口返回值
     */
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }
}
