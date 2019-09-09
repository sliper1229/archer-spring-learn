package com.luo.spring.framework.aop.intercept;

import com.luo.spring.framework.aop.aspect.ArchJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luoxuzheng
 * @create 2019-09-09 8:05
 **/
public class ArchMethodInvocation implements ArchJoinPoint {

    private Object proxy;
    private Method method;
    private Object target;
    private Object [] arguments;
    private List<Object> interceptorsAndDynamicMethodMatchers;
    private Class<?> targetClass;

    private Map<String, Object> userAttributes;

    //定义一个索引，从-1开始来记录当前拦截器执行的位置
    private int currentInterceptorIndex = -1;

    public ArchMethodInvocation(
            Object proxy, Object target, Method method, Object[] arguments,
            Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable {

        //如果Interceptor执行完了，则执行joinPoint
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target,this.arguments);
        }

        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);

        //如果要动态匹配joinPoint
        if (interceptorOrInterceptionAdvice instanceof ArchMethodInterceptor) {

            ArchMethodInterceptor mi =
                    (ArchMethodInterceptor) interceptorOrInterceptionAdvice;

            return mi.invoke(this);

        } else {
            //动态匹配失败时,略过当前Intercetpor,调用下一个Interceptor
            return proceed();
        }
    }


    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<String,Object>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }

    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }
}
