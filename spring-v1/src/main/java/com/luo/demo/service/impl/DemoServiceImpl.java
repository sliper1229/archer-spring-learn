package com.luo.demo.service.impl;

import com.luo.demo.service.DemoService;
import com.luo.springmvcframework.annotation.ArchService;

/**
 * service实现
 *
 * @author luoxuzheng
 * @create 2019-08-28 15:16
 **/
@ArchService
public class DemoServiceImpl implements DemoService {
    @Override
    public String get(String name) {
        return "my name is "+ name;
    }
}
