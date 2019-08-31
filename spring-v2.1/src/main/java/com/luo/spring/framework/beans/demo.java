package com.luo.spring.framework.beans;

import java.io.Serializable;

/**
 * @author luoxuzheng
 * @create 2019-08-31 10:03
 **/
public class demo implements Serializable {


    public static void main(String[] args) {


        getClassName();
    }

    private static void getClassName() {
        //得到类的简写名称
        System.out.println(demo.class.getSimpleName());

        //得到对象的全路径
        System.out.println(demo.class);

        //得到对象的类模板示例，也就是Class
        System.out.println(demo.class.getClass());

        //得到Class类的名称
        System.out.println(demo.class.getClass().getName());

        System.out.println(demo.class.getName());

        Class<?>[] interfaces = demo.class.getInterfaces();
        for (Class<?> i : interfaces) {
            //如果是多个实现类，只能覆盖
            //为什么？因为Spring没那么智能，就是这么傻
            //这个时候，可以自定义名字
            System.out.println(i.getName());
        }
    }
}
