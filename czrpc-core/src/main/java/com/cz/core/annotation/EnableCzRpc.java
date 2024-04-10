package com.cz.core.annotation;

import com.cz.core.config.consumer.ConsumerConfig;
import com.cz.core.config.provider.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 组合一个入口.
 * 启用 rpc 服务
 *
 * @author Zjianru
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableCzRpc {
}
