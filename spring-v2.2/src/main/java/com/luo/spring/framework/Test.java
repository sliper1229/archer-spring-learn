package com.luo.spring.framework;

import com.luo.spring.demo.service.impl.QueryService;
import com.luo.spring.framework.context.ArchApplicationContext;

/**
 * 测试类
 *
 * @author luoxuzheng
 * @create 2019-08-31 12:29
 **/
public class Test {

    public static void main(String[] args) {
        ArchApplicationContext context = new ArchApplicationContext("classpath:application.properties");
        try {
//            Object queryService = context.getBean("queryService");
//            Object queryServiceType = context.getBean(QueryService.class);
//            System.out.println(queryService);
//            System.out.println(queryServiceType);
            Object myAction = context.getBean("myAction");
            System.out.println(myAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
