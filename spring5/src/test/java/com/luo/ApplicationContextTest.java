package com.luo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Tom.
 */
public class ApplicationContextTest {
    public static void main(String[] args) {

//        ClassPathResource res = new ClassPathResource("a.xml,b.xml");
//        ClassPathResource res =new ClassPathResource(new String[]{"a.xml","b.xml"});

        ApplicationContext app = new ClassPathXmlApplicationContext("classpath:application-context.xml");


    }
}
