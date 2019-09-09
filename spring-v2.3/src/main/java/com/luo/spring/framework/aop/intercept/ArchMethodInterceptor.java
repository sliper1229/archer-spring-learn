package com.luo.spring.framework.aop.intercept;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:04
 **/
public interface ArchMethodInterceptor {
    Object invoke(ArchMethodInvocation invocation) throws Throwable;
}
