package com.luo.spring.framework.aop.support;

import com.luo.spring.framework.aop.aspect.ArchAfterReturningAdviceInterceptor;
import com.luo.spring.framework.aop.aspect.ArchAfterThrowingAdviceInterceptor;
import com.luo.spring.framework.aop.aspect.ArchMethodBeforeAdviceInterceptor;
import com.luo.spring.framework.aop.config.ArchAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author luoxuzheng
 * @create 2019-09-09 7:58
 **/
public class ArchAdviseSupport {
    private Class<?> targetClass;

    private Object target;

    private ArchAopConfig config;

    private Pattern pointCutClassPattern;

    private transient Map<Method, List<Object>> methodCache;

    public ArchAdviseSupport(ArchAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass(){
        return this.targetClass;
    }

    public Object getTarget(){
        return this.target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception{
        List<Object> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());

            cached = methodCache.get(m);

            //底层逻辑，对代理方法进行一个兼容处理
            this.methodCache.put(m,cached);
        }

        return cached;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");
        //pointCut=public .* com.gupaoedu.vip.spring.demo.service..*Service..*(.*)
        //玩正则
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {

            methodCache = new HashMap<Method, List<Object>>();
            Pattern pattern = Pattern.compile(pointCut);



            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String,Method>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(),m);
            }

            for (Method m : this.targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pattern.matcher(methodString);
                if(matcher.matches()){
                    //执行器链
                    List<Object> advices = new LinkedList<Object>();
                    //把每一个方法包装成 MethodIterceptor
                    //before
                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        //创建一个Advivce
                        advices.add(new ArchMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()),aspectClass.newInstance()));
                    }
                    //after
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        //创建一个Advivce
                        advices.add(new ArchAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()),aspectClass.newInstance()));
                    }
                    //afterThrowing
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        //创建一个Advivce
                        ArchAfterThrowingAdviceInterceptor throwingAdvice =
                                new ArchAfterThrowingAdviceInterceptor(
                                        aspectMethods.get(config.getAspectAfterThrow()),
                                        aspectClass.newInstance());
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(throwingAdvice);
                    }
                    methodCache.put(m,advices);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
