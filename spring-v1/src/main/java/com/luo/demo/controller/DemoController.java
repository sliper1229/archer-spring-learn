package com.luo.demo.controller;

import com.luo.demo.service.DemoService;
import com.luo.springmvcframework.annotation.ArchAutowired;
import com.luo.springmvcframework.annotation.ArchController;
import com.luo.springmvcframework.annotation.ArchRequestMapping;
import com.luo.springmvcframework.annotation.ArchRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * demoController
 *
 * @author luoxuzheng
 * @create 2019-08-28 15:15
 **/
@ArchController
@ArchRequestMapping("demo")
public class DemoController {

    @ArchAutowired
    private DemoService demoService;

    @ArchRequestMapping("query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @ArchRequestParam("name") String name) {
        String result = demoService.get(name);
//		String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ArchRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @ArchRequestParam("a") Integer a, @ArchRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ArchRequestMapping("/remove")
    public String remove(@ArchRequestParam("id") Integer id) {
        return id + "";
    }

}
