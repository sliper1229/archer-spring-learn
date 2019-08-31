package com.luo.spring.framework.beans.support;

import com.luo.spring.framework.beans.config.ArchBeanDefinition;
import com.luo.spring.framework.context.support.ArchAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luoxuzheng
 * @create 2019-08-31 10:30
 **/
public class ArchDefaultListableBeanFactory extends ArchAbstractApplicationContext {
    //存储注册信息的BeanDefinition
    protected final Map<String, ArchBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
