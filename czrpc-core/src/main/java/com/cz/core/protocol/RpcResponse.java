package com.cz.core.protocol;

import com.cz.core.ex.RpcException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * rpc - 返回信息包装
 * response data for RPC call.
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    boolean status;
    T data;
    RpcException exception;
}
