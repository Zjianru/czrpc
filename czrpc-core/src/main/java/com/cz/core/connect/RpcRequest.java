package com.cz.core.connect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * rpc - 请求信息包装
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class RpcRequest implements Serializable {
    @NotNull
    private Class service;
    @NotNull
    private String method;
    private String methodSign;
    @NotNull
    private Object[] args;
    @NotNull
    private Class<?>[] argsType;

}
