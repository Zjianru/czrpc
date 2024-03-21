package com.cz.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * code desc
 *
 * @author Zjianru
 */
@Data
public class ProviderMeta {
    Method method;
    String methodSign;
    Object targetService;
}
