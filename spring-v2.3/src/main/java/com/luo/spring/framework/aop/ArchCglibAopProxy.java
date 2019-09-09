package com.luo.spring.framework.aop;

import com.luo.spring.framework.aop.support.ArchAdviseSupport;

/**
 * cglib代理
 *
 * @author luoxuzheng
 * @create 2019-09-09 7:57
 **/
public class ArchCglibAopProxy implements ArchAopProxy {


    public ArchCglibAopProxy(ArchAdviseSupport config) {
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
