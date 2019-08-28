package com.luo.springmvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author luoxuzheng
 * @create 2019-08-28 15:29
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArchRequestParam {
    String value() default "";
}
