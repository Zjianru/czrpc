package com.cz.demo.provider.controller;

import com.cz.core.protocol.RpcResponse;
import com.cz.demo.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 请求转化为 RPC 请求
 * 完成接口测试
 *
 * @author Zjianru
 */
@RestController
//@EnableCzConfig
public class ProviderController {


    @Autowired
    UserService userService;


    @RequestMapping("/ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }

}
