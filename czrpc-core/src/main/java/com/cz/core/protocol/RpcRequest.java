package com.cz.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * rpc - 请求信息包装
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private Class service;
    private Method method;
    private String methodName;
    private String methodSign;
    private Object[] args;
    private Class<?>[] argsType;

}
