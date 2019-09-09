package com.luo.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * 切入点
 *
 * @author luoxuzheng
 * @create 2019-09-09 8:05
 **/
public interface ArchJoinPoint {

    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
