package com.luo.spring.framework.context.support;

/**
 * IOC容器实现的顶层设计
 * @author luoxuzheng
 * @create 2019-08-31 10:31
 **/
public abstract class ArchAbstractApplicationContext {
    //受保护，只提供给子类重写
    public void refresh() throws Exception {}

}
