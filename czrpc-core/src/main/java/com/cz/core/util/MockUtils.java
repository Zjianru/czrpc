package com.cz.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * 挡板工具
 *
 * @author Zjianru
 */
public class MockUtils {
    public static Object mock(Class<?> type) {
        // 模拟基本类型
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10000L;
        }
        if (Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if (type.equals(String.class)) {
            return "this_is_a_mock_string";
        }
        // 模拟对象类型
        return mockPojo(type);
    }

    /**
     * 对象类型模拟
     *
     * @param type 返回值类型
     * @return 模拟对象
     */
    @SneakyThrows
    private static Object mockPojo(Class<?> type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fType = field.getType();
            Object fValue = mock(fType);
            field.set(result, fValue);
        }
        return result;
    }

//    public static void main(String[] args) {
//        System.out.println(mock(UserDto.class));
//    }
//
//    public static class UserDto{
//        private int a;
//        private String b;
//
//        @Override
//        public String toString() {
//            return a + "," + b;
//        }
//    }
}