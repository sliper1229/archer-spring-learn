package com.luo.spring.framework.beans.config;

import lombok.Data;

/**
 * 统一各种配置格式的bean的封装
 * @author luoxuzheng
 * @create 2019-08-31 10:36
 **/
@Data
public class ArchBeanDefinition {

    //bean的全路径，包括包名 如：com.luo.spring.framework.beans.config.ArchBeanDefinition
    private String beanClassName;
    private boolean lazyInit = false;
    //bean的名称 如：archBeanDefinition，用来表示bean的自己定义的规则
    private String factoryBeanName;

}
