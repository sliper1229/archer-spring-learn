package com.luo.springmvcframework.servlet.v2;

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

/**
 * 面向过程的升级版本v2，采用设计模式对其进行升级
 *
 * @author luoxuzheng
 * @create 2019-08-28 15:53
 **/
public class ArchDispatcherServlet extends HttpServlet {

    //存放resouces下的配置文件信息的容器
    private Properties contextConfig = new Properties();

    //存储所有扫描到的类
    private List<String> classNames = new ArrayList<String>();

    //IOC容器，保存所有实例化对象
    //注册式单例模式
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //保存Contrller中所有Mapping的对应关系
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    //模板模式重写doPost方法
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

    /**
     * 委派模式，派发分任务给具体执行业务逻辑的controller
     *
     * @param req
     * @param resp
     */
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!");
            return;
        }

        Method method = this.handlerMapping.get(url);
        //第一个参数：方法所在的实例
        //第二个参数：调用时所需要的实参
        Map<String, String[]> params = req.getParameterMap();
        //获取方法的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //保存请求的url参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();
        //保存赋值参数的位置
        Object[] paramValues = new Object[parameterTypes.length];
        //按根据参数位置动态赋值
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            } else if (parameterType == String.class) {

                //提取方法中加了注解的参数//这里的代码有问题
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {
                        if (a instanceof ArchRequestParam) {
                            String paramName = ((ArchRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }

            }
        }

        //投机取巧的方式
        //通过反射拿到method所在class，拿到class之后还是拿到class的名称
        //再调用toLowerFirstCase获得beanName
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName), paramValues);

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
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(ArchController.class)) {
                continue;
            }

            String baseUrl = "";
            //1、获取Controller的url配置
            if (clazz.isAnnotationPresent(ArchRequestMapping.class)) {
                ArchRequestMapping requestMapping = clazz.getAnnotation(ArchRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //2、获取Method的url配置
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {

                //没有加RequestMapping注解的直接忽略
                if (!method.isAnnotationPresent(ArchRequestMapping.class)) {
                    continue;
                }


                ///3、映射URL，将多个（或者没有/) / 变成一个 (//demo//query变成/demo/query)
                ArchRequestMapping requestMapping = method.getAnnotation(ArchRequestMapping.class);

                String url = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped " + url + "," + method);
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
