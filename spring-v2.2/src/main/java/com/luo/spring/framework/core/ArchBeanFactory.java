package com.luo.spring.framework.core;

/**
 * IOC顶层容器
 *
 * @author luoxuzheng
 * @create 2019-08-31 10:19
 **/
public interface ArchBeanFactory {
    /**
     * 根据beanName从IOC容器中获得一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 根据ClassType获取实例
     * @param beanClass
     * @return
     * @throws Exception
     */
    Object getBean(Class<?> beanClass) throws Exception;
}
