package com.cz.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodUtils {
    public MethodUtils() {
    }

    /**
     * 判断是否为本地方法
     *
     * @param method 方法名
     * @return 是否为本地方法
     */
    public static boolean isLocalMethod(final String method) {
        return "toString".equals(method) ||
                "clone".equals(method) ||
                "hashCode".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notifyAll".equals(method) ||
                "notify".equals(method);
    }

    /**
     * 判断是否为本地方法
     *
     * @param method 方法名
     * @return 是否为本地方法
     */
    public static boolean isLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    /**
     * 组成方法签名
     *
     * @param method method
     * @return string like "methodName@parameterCount_parameterType1_parameterType2..."
     */
    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

}