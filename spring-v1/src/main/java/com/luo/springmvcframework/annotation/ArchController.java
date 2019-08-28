package com.luo.springmvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author luoxuzheng
 * @create 2019-08-28 15:24
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArchController {
    String value() default "";
}
