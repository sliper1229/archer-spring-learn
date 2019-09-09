package com.luo.spring.framework.aop.aspect;

import com.luo.spring.framework.aop.intercept.ArchMethodInterceptor;
import com.luo.spring.framework.aop.intercept.ArchMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:17
 **/
public class ArchAfterReturningAdviceInterceptor extends ArchAbstractAspectAdvice implements ArchAdvice, ArchMethodInterceptor {
    private ArchJoinPoint joinPoint;

    public ArchAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(ArchMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
