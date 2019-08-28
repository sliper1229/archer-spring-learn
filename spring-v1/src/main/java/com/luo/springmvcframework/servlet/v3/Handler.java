package com.luo.springmvcframework.servlet.v3;

import com.luo.springmvcframework.annotation.ArchRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author luoxuzheng
 * @create 2019-08-28 18:02
 **/
public class Handler {
    protected Object controller;	//保存方法对应的实例
    protected Method method;		//保存映射的方法
    protected Pattern pattern;
    protected Map<String,Integer> paramIndexMapping;	//参数顺序

    /**
     * 构造一个Handler基本的参数
     * @param controller
     * @param method
     */
    protected Handler(Pattern pattern,Object controller,Method method){
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;

        paramIndexMapping = new HashMap<String,Integer>();
        putParamIndexMapping(method);
    }

    private void putParamIndexMapping(Method method){

        //提取方法中加了注解的参数
        Annotation[] [] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof ArchRequestParam){
                    String paramName = ((ArchRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //提取方法中的request和response参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class ||
                    type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }
    }
}
