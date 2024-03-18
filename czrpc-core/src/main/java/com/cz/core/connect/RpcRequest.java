package com.cz.core.connect;

import lombok.Data;

@Data
public class RpcRequest {
    private Class service;
    private String method;
    private Object[] args;

}
