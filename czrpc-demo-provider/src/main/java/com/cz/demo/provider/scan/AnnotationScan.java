package com.cz.demo.provider.scan;


import com.cz.czrpc.core.annotation.czProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AnnotationScan {
    @Autowired
    ApplicationContext context;
    public final Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void scan() {
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(czProvider.class);
        beansWithAnnotation.values().forEach(this::getInterface);
    }

    private void getInterface(Object bean) {
        Class<?> anInterface = bean.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), bean);
    }

}
