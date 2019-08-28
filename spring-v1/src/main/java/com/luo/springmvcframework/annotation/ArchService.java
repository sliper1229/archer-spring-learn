package com.luo.springmvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author luoxuzheng
 * @create 2019-08-28 15:27
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArchService {
    String value() default "";
}
