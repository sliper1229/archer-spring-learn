package com.luo.spring.framework.aop;

/**
 * 获取代理类的接口
 *
 * @author luoxuzheng
 * @create 2019-09-09 7:55
 **/
public interface ArchAopProxy {

    Object getProxy();


    Object getProxy(ClassLoader classLoader);
}
