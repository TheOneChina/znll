package com.web.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IfNoneMatch {
    /**
     * TODO: SpEL expression to calculate ETag value
     *
     * @return ETag for request
     */
    String value() default "";
}
