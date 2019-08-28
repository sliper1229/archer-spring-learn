package com.luo.springmvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author luoxuzheng
 * @create 2019-08-28 15:29
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArchAutowired {
    String value() default "";
}
