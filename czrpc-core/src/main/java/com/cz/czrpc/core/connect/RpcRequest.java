package com.cz.czrpc.core.connect;

import lombok.Data;

@Data
public class RpcRequest {
    private Class service;
    private String method;
    private Object[] args;

}
