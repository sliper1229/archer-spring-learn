package com.luo.springmvcframework.servlet.v1;

import com.luo.springmvcframework.annotation.ArchAutowired;
import com.luo.springmvcframework.annotation.ArchController;
import com.luo.springmvcframework.annotation.ArchRequestMapping;
import com.luo.springmvcframework.annotation.ArchService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 委派任务给各controller
 * 面向过程编程，代码很不优雅
 *
 * @author luoxuzheng
 * @create 2019-08-28 15:21
 **/
public class ArchDispatcherServlet extends HttpServlet {

    private Map<String, Object> mapping = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        //获取是相路径 如：/demo/query
        String url = req.getRequestURI();
        //获取服务器地址 例如 http://localhost:8080  获取到的是"",无用
        String contextPath = req.getContextPath();
        //取相对地址
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!this.mapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!");
            return;
        }

        //委派模式，反射调用
        Method method = (Method) this.mapping.get(url);
        Map<String, String[]> params = req.getParameterMap();

        //暂时写死
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, params.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");

            //扫描package下的注解
            doScanner(scanPackage);

            //实例化扫描到的类
            for (String className : mapping.keySet()) {
                if (!className.contains(".")) continue;

                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(ArchController.class)) {
                    mapping.put(className, clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(ArchRequestMapping.class)) {
                        ArchRequestMapping requestMapping = clazz.getAnnotation(ArchRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(ArchRequestMapping.class)) {
                            continue;
                        }
                        ArchRequestMapping requestMapping = method.getAnnotation(ArchRequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                } else if (clazz.isAnnotationPresent(ArchService.class)) {
                    ArchService service = clazz.getAnnotation(ArchService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.newInstance();
                    mapping.put(beanName, instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
            for (Object object : mapping.values()) {
                if (object == null) {
                    continue;
                }
                Class clazz = object.getClass();
                if (clazz.isAnnotationPresent(ArchController.class)) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (!field.isAnnotationPresent(ArchAutowired.class)) {
                            continue;
                        }
                        ArchAutowired autowired = field.getAnnotation(ArchAutowired.class);
                        String beanName = autowired.value();
                        if ("".equals(beanName)) {
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        try {
                            field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("Arch MVC Framework is init");

    }

    private void doScanner(String scanPackage) {
        //扫描的是target目录
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (!file.isDirectory()) {
                //非class文件不处理
                if (!file.getName().endsWith(".class")) continue;

                //获取类名，存到内存map当中，类名包含包名
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                mapping.put(clazzName, null);
            } else {
                doScanner(scanPackage + "." + file.getName());
            }
        }
    }
}
