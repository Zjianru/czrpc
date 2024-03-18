package com.cz.core.connect;

import lombok.Data;

/**
 * rpc - 请求信息包装
 *
 * @author Zjianru
 */
@Data
public class RpcRequest {
    private Class service;
    private String method;
    private Object[] args;

}
