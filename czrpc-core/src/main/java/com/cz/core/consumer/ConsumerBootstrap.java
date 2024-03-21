package com.cz.core.consumer;

import com.cz.core.annotation.czConsumer;
import com.cz.core.consumer.proxy.ConsumerProxyFactory;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.util.*;

/**
 * consumer 启动并完成注册
 *
 * @author Zjianru
 */
@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动消费者
     * 收集目前使用到的提供者服务信息
     */
    public void start() {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = findAnnotatedField(bean.getClass());
            fields.forEach(field -> {
                try {
                    Class<?> service = field.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = ConsumerProxyFactory.createByJDK(service);
                        stub.put(serviceName, consumer);
                    }
                    field.setAccessible(true);
                    field.set(bean, consumer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 查找被注解的字段
     *
     * @param clazz 类
     * @return List<Field>
     */

    private List<Field> findAnnotatedField(Class<?> clazz) {
        // 取到的是被代理增强的子类
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            List<Field> fieldInCurrentClass = Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(czConsumer.class))
                    .toList();
            result.addAll(fieldInCurrentClass);
            clazz = clazz.getSuperclass();
        }
        return result;
    }


}
