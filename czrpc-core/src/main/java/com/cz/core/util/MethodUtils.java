package com.cz.core.util;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理反射相关逻辑的工具类
 */
public class MethodUtils {
    public MethodUtils() {
    }

    private static final String serviceSeparator = "@";
    private static final String methodSeparator = "_";


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
        sb.append(serviceSeparator).append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append(methodSeparator).append(c.getCanonicalName())
        );
        return sb.toString();
    }

    /**
     * 查找被注解的字段
     *
     * @param clazz 类
     * @return List<Field>
     */
    public static List<Field> findAnnotatedField(Class<?> clazz,
                                                 Class<? extends Annotation> annotationClass) {
        // 取到的是被代理增强的子类
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            List<Field> fieldInCurrentClass = Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(annotationClass))
                    .toList();
            result.addAll(fieldInCurrentClass);
            clazz = clazz.getSuperclass();
        }
        return result;
    }

}