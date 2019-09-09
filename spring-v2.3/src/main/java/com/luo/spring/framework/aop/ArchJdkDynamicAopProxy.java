package com.luo.spring.framework.aop;

import com.luo.spring.framework.aop.intercept.ArchMethodInvocation;
import com.luo.spring.framework.aop.support.ArchAdviseSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * jdk的动态代理
 * @author luoxuzheng
 * @create 2019-09-09 7:59
 **/
public class ArchJdkDynamicAopProxy implements ArchAopProxy, InvocationHandler {

    private ArchAdviseSupport archAdviseSupport;


    public ArchJdkDynamicAopProxy(ArchAdviseSupport config){
        this.archAdviseSupport = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.archAdviseSupport.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader,this.archAdviseSupport.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = this.archAdviseSupport.
                getInterceptorsAndDynamicInterceptionAdvice(method,this.archAdviseSupport.getTargetClass());

        ArchMethodInvocation invocation = new ArchMethodInvocation(proxy,this.archAdviseSupport.getTarget(),
                method,args,this.archAdviseSupport.getTargetClass(),interceptorsAndDynamicMethodMatchers);

        return invocation.proceed();
    }
}
