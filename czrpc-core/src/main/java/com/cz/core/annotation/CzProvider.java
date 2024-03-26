package com.cz.core.annotation;

import java.lang.annotation.*;

/**
 * czrpc - annotation
 * 将资源标记为提供者
 *
 * @author Zjianru
 */
@Inherited
@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CzProvider {
}
