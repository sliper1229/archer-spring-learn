package com.luo.spring.framework.aop.aspect;

import com.luo.spring.framework.aop.intercept.ArchMethodInterceptor;
import com.luo.spring.framework.aop.intercept.ArchMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:17
 **/
public class ArchAfterThrowingAdviceInterceptor extends ArchAbstractAspectAdvice implements ArchAdvice, ArchMethodInterceptor {
    private String throwingName;

    public ArchAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(ArchMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName){
        this.throwingName = throwName;
    }
}
