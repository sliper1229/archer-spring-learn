package com.luo.spring.framework.beans.config;

/**
 * @author luoxuzheng
 * @create 2019-08-31 12:02
 **/
public class ArchBeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}
