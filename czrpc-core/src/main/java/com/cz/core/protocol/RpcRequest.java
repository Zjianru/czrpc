package com.cz.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * rpc - 请求信息包装
 *
 * @author Zjianru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcRequest implements Serializable {
    private Class<?> service;
    private String methodName;
    private String methodSign;
    private Object[] args;
    private Class<?>[] argsType;

}
