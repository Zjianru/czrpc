package com.cz.core.annotation;

import java.lang.annotation.*;

/**
 * czrpc - annotation
 * 将资源标记为消费者
 *
 * @author Zjianru
 */
@Inherited
@Target({ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CzConsumer {
}
