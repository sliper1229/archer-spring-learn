package com.luo.spring.framework.aop.aspect;

import com.luo.spring.framework.aop.intercept.ArchMethodInterceptor;
import com.luo.spring.framework.aop.intercept.ArchMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:17
 **/
public class ArchMethodBeforeAdviceInterceptor extends ArchAbstractAspectAdvice implements ArchAdvice, ArchMethodInterceptor {

    private ArchJoinPoint joinPoint;
    public ArchMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method,Object[] args,Object target) throws Throwable{
        //传送了给织入参数
        //method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint,null,null);

    }
    @Override
    public Object invoke(ArchMethodInvocation mi) throws Throwable {
        //从被织入的代码中才能拿到，JoinPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
