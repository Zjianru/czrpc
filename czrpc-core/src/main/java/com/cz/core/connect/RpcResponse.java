package com.cz.core.connect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rpc - 返回信息包装
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {
    boolean status;
    T data;
}
