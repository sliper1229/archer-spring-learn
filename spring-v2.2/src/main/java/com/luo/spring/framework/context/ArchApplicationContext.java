package com.luo.spring.framework.context;

import com.luo.spring.framework.annotation.ArchAutowired;
import com.luo.spring.framework.annotation.ArchController;
import com.luo.spring.framework.annotation.ArchService;
import com.luo.spring.framework.beans.ArchBeanWrapper;
import com.luo.spring.framework.beans.config.ArchBeanDefinition;
import com.luo.spring.framework.beans.config.ArchBeanPostProcessor;
import com.luo.spring.framework.beans.support.ArchBeanDefinitionReader;
import com.luo.spring.framework.beans.support.ArchDefaultListableBeanFactory;
import com.luo.spring.framework.core.ArchBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 源码分析的套路，IOC、DI、MVC、AOP
 * @author luoxuzheng
 * @create 2019-08-31 10:24
 **/
public class ArchApplicationContext extends ArchDefaultListableBeanFactory implements ArchBeanFactory {

    //配置信息的地址的字符串形式
    private String[] configLoactions;

    //读取配置信息
    private ArchBeanDefinitionReader reader;

    //单例的IOC容器缓存
    private Map<String,Object> singletonObjects = new ConcurrentHashMap<String, Object>();

    //通用的IOC容器
    private Map<String, ArchBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, ArchBeanWrapper>();


    public ArchApplicationContext(String... configLoactions){
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  //1、IOC的实现   三步骤 1、定位，2、加载，3、注册
     *
     * @throws Exception
     */
    @Override
    public void refresh() throws Exception {

        //1、定位 定位配置文件
        reader = new ArchBeanDefinitionReader(this.configLoactions);


        //2、加载 加载配置文件，扫描相关的类，将他们封装成BeanDefinition
        //有多个ArchBeanDefinition里面包含的beanClassName一样，但是factoryBeanName不一样
        List<ArchBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


        //3、注册 把配置信息放到容器里面（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);


        //4、把不是延时加载的类，初始化
        doAutowrited();
    }

    /**
     * 把不是延时加载的类，初始化
     */
    private void doAutowrited() {
        for (Map.Entry<String, ArchBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    /**
     * 把ArchBeanDefinition里面的信息放到伪IOC容器 ArchDefaultListableBeanFactory 的 beanDefinitionMap 里面
     * @param beanDefinitions
     * @throws Exception
     */
    private void doRegisterBeanDefinition(List<ArchBeanDefinition> beanDefinitions) throws Exception {

        for (ArchBeanDefinition beanDefinition: beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        //到这里为止，容器初始化完毕
    }


    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(toLowerFirstCase(beanClass.getSimpleName()));
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 依赖注入，从这里开始，通过读取BeanDefinition中的信息
     * 然后，通过反射机制创建一个实例并返回
     * Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
     * 装饰器模式：
     * 1、保留原来的OOP关系
     * 2、我需要对它进行扩展，增强（为了以后AOP打基础）
     * @param beanName
     * @return
     * @throws Exception
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        ArchBeanDefinition archBeanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;

        //这个逻辑还不严谨，自己可以去参考Spring源码
        //工厂模式 + 策略模式
        ArchBeanPostProcessor postProcessor = new ArchBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(instance,beanName);

        //1、初始化
        //class A{ B b;}
        //class B{ A a;}
        //先有鸡还是先有蛋的问题，一个方法是搞不定的，要分两次
        instance = instantiateBean(beanName,archBeanDefinition);

        //2、把这个对象封装到BeanWrapper中
        ArchBeanWrapper beanWrapper = new ArchBeanWrapper(instance);

        //singletonObjects

        //factoryBeanInstanceCache


        //3、拿到BeanWraoper之后，把BeanWrapper保存到IOC容器中去
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);
        postProcessor.postProcessAfterInitialization(instance,beanName);

        //3、注入
        populateBean(beanName,new ArchBeanDefinition(),beanWrapper);


        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    /**
     * 注入
     * @param beanName
     * @param archBeanDefinition
     * @param beanWrapper
     */
    private void populateBean(String beanName, ArchBeanDefinition archBeanDefinition, ArchBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();

//        archBeanDefinition.getBeanClassName();

        Class<?> clazz = beanWrapper.getWrappedClass();
        //判断只有加了注解的类，才执行依赖注入
        if(!(clazz.isAnnotationPresent(ArchController.class) || clazz.isAnnotationPresent(ArchService.class))){
            return;
        }

        //获得所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(ArchAutowired.class)){ continue;}

            ArchAutowired autowired = field.getAnnotation(ArchAutowired.class);

            String autowiredBeanName =  autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }

            //强制访问
            field.setAccessible(true);

            try {
                //为什么会为NULL，先留个坑
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){ continue; }
//                if(instance == null){
//                    continue;
//                }
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 初始化bean
     * @param beanName
     * @param archBeanDefinition
     * @return
     */
    private Object instantiateBean(String beanName, ArchBeanDefinition archBeanDefinition) {
        //1、拿到要实例化的对象的类名
        String className = archBeanDefinition.getBeanClassName();

        //2、反射实例化，得到一个对象
        Object instance = null;
        try {
//            archBeanDefinition.getFactoryBeanName()
            //假设默认就是单例,细节暂且不考虑，先把主线拉通
            if(this.singletonObjects.containsKey(className)){
                instance = this.singletonObjects.get(className);
            }else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.singletonObjects.put(className,instance);
                this.singletonObjects.put(archBeanDefinition.getFactoryBeanName(),instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return instance;
    }

}
