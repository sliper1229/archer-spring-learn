package com.luo.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @author luoxuzheng
 * @create 2019-08-31 14:53
 **/
public class ArchModelAndView {
    private String viewName;
    private Map<String,?> model;

    public ArchModelAndView(String viewName) { this.viewName = viewName; }

    public ArchModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

//    public void setViewName(String viewName) {
//        this.viewName = viewName;
//    }

    public Map<String, ?> getModel() {
        return model;
    }

//    public void setModel(Map<String, ?> model) {
//        this.model = model;
//    }
}
