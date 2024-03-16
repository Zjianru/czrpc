package com.cz.czrpc.core.connect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {
    boolean status;
    T data;
}
