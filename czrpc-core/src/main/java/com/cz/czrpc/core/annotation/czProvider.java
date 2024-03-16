package com.cz.czrpc.core.annotation;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface czProvider {
}
