package com.cz.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 服务提供者元信息
 *
 * @author Zjianru
 */
@Data
@Builder
public class ProviderMeta {
    /**
     * 能力方法
     */
    Method method;
    /**
     * 方法签名
     */
    String methodSign;
    /**
     * 实例 bean
     */
    Object targetService;
}
