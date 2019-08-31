package com.luo.spring.framework.beans;

/**
 * @author luoxuzheng
 * @create 2019-08-31 12:02
 **/
public class ArchBeanWrapper {
    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public ArchBeanWrapper(Object wrappedInstance){
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance(){
        return this.wrappedInstance;
    }

    // 返回代理以后的Class
    // 可能会是这个 $Proxy0
    public Class<?> getWrappedClass(){
        return this.wrappedInstance.getClass();
    }
}
