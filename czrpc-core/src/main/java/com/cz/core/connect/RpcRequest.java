package com.cz.core.connect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rpc - 请求信息包装
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    private Class service;
    private String method;
    private Object[] args;
    private Class<?>[] argsType;

}
