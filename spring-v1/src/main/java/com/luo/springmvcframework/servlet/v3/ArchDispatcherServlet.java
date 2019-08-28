package com.luo.springmvcframework.servlet.v3;

import com.luo.springmvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设计模式升级版的升级版本v3，相对于v2，是对handlerMapping做的优化，用类代替map，遵循最少知道原则和单一职责原则
 *
 * @author luoxuzheng
 * @create 2019-08-28 15:53
 **/
public class ArchDispatcherServlet extends HttpServlet {

    private static final String LOCATION = "contextConfigLocation";

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String,Object> ioc = new HashMap<String,Object>();

    //保存所有的Url和方法的映射关系
    private List<Handler> handlerMapping = new ArrayList<Handler>();

    public ArchDispatcherServlet(){ super(); }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //委派模式，分发任务
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Excetion Detail:" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IllegalAccessException, IOException, InvocationTargetException {
        try{
            Handler handler = getHandler(req);

            if(handler == null){
                //如果没有匹配上，返回404错误
                resp.getWriter().write("404 Not Found");
                return;
            }


            //获取方法的参数列表
            Class<?> [] paramTypes = handler.method.getParameterTypes();

            //保存所有需要自动赋值的参数值
            Object [] paramValues = new Object[paramTypes.length];


            Map<String,String[]> params = req.getParameterMap();
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "");
                        //用,替代空格.replaceAll(",\\s", ",");

                //如果找到匹配的对象，则开始填充参数值
                if(!handler.paramIndexMapping.containsKey(param.getKey())) continue;
                int index = handler.paramIndexMapping.get(param.getKey());
                paramValues[index] = convert(paramTypes[index],value);
            }


            //设置方法中的request和response对象
            if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
                int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
                paramValues[reqIndex] = req;
            }

            if(handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
                int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
                paramValues[respIndex] = resp;
            }


            Object invoke = handler.method.invoke(handler.controller, paramValues);
            if(!handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
                resp.getWriter().write(invoke.toString());
            }


        }catch(Exception e){
            throw e;
        }
    }

    /**
     * 参数转换，可以借鉴Spring的Convert策略模式来实现  interface Converter<S, T>
     * @param paramType
     * @param value
     * @return
     */
    private Object convert(Class<?> paramType, String value) {
        if(Integer.class == paramType){
            return Integer.valueOf(value);
        }
        //如果还有double或者其他类型，继续加if
        //这时候，我们应该想到策略模式了
        //在这里暂时不实现，希望小伙伴自己来实现

        return value;
    }

    private Handler getHandler(HttpServletRequest req) {
        if(handlerMapping.isEmpty()){ return null; }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (Handler handler : handlerMapping) {
            try{
                Matcher matcher = handler.pattern.matcher(url);
                //如果没有匹配上继续下一个匹配
                if(!matcher.matches()) continue;

                return handler;
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        //模板模式
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化所有相关的类的实例，并放到相关的容器中
        doInstance();

        //4、完成依赖注入
        doAutowired();

        //5、初始化HandMapping
        initHandlerMapping();

        //6、结束
        System.out.println("Arch Spring framework is init.");
    }

    /**
     * 初始化HandMapping
     */
    private void initHandlerMapping() {

        if(ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(ArchController.class)) continue;

            String url = "";
            //获取Controller的url配置
            if(clazz.isAnnotationPresent(ArchRequestMapping.class)){
                ArchRequestMapping requestMapping = clazz.getAnnotation(ArchRequestMapping.class);
                url = requestMapping.value();
            }

            //获取Method的url配置
            Method [] methods = clazz.getMethods();
            for (Method method : methods) {

                //没有加RequestMapping注解的直接忽略
                if(!method.isAnnotationPresent(ArchRequestMapping.class)) continue;

                //映射URL
                ArchRequestMapping requestMapping = method.getAnnotation(ArchRequestMapping.class);
                String regex = ("/" + url + "/" + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(pattern,entry.getValue(),method));
                System.out.println("mapping " + regex + "," + method);
            }
        }
    }

    /**
     * 完成依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //拿到实例对象中的所有属性(包括所有访问权限的)
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(ArchAutowired.class)) {
                    continue;
                }
                ArchAutowired autowired = field.getAnnotation(ArchAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                //不管你愿不愿意，强吻
                field.setAccessible(true); //设置私有属性的访问权限
                try {
                    //执行注入动作
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * 初始化所有相关的类的实例，并放到相关的容器中
     * 工厂模式来实现的
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(ArchController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(ArchService.class)) {
                    //1、默认的类名首字母小写

                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    //2、自定义命名
                    ArchService service = clazz.getAnnotation(ArchService.class);
                    if (!"".equals(service.value())) {
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3、根据类型注入实现类，投机取巧的方式
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The beanName is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将类名首字母转换成小写，利用assic码
     *
     * @param simpleName
     * @return
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 扫描相关的类
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        //1、获取到磁盘的文件路径
        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        //打印看一下url是什么
        //todo
        System.out.println("url" + url);
        System.out.println("url.getFile()" + url.getFile());

        //2、遍历包scanPackage目录下的文件，并将类名存到容器classNames中
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName()).replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件
     *
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream fis = null;
        try {
            //1、将配置文件application.properties读到流里
            fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            //2、读取配置文件
            contextConfig.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3、关闭流
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
