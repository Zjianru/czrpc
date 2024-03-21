package com.cz.core.consumer.util;

public class MethodUtils {
    public MethodUtils() {
    }

    /**
     * 判断是否为本地方法
     *
     * @param method 方法名
     * @return 是否为本地方法
     */
    public static boolean isLocalMethod(String method) {
        return "toString".equals(method) ||
                "clone".equals(method) ||
                "hashCode".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notifyAll".equals(method) ||
                "notify".equals(method);
    }
}