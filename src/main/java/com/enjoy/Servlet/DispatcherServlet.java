package com.enjoy.Servlet;


//import org.omg.CORBA.Object;

import com.enjoy.annotion.*;
import com.enjoy.annotion.EnjoyController;
import com.enjoy.annotion.RequestMapping;
import com.enjoy.controller.MyController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    List<String> classNames = new ArrayList<String>();
    Map<String, Object> beans = new HashMap<>();
    //    定义映射关系
    Map<String, Object> handlemap = new HashMap<>();
//    定义映射关系


    //tomcat创建的时候实例化map
    @Override
    public void init(ServletConfig config) {
        basePackageScan("com.enjoy");

        //对className进行实例化
        doInstance();
        //依赖注入
        doAutowired();
        //连接匹配
        doUrlMapping();

    }

    /**
     * SpringMvc
     *连接匹配 让@RequestMapping("/value/value")中的连接能够对应到存在IOC容器中的方法上.
     */
    public void doUrlMapping() {
//         Map<String, Object> beans 保存着所有注入的Bean
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            //遍历map取得bean
            Object instance = entry.getValue();
            //取得每个bean的名字
            Class<?> aClass = instance.getClass();
//            确定在类上是否有@RequestMapping
            if (aClass.isAnnotationPresent(RequestMapping.class)) {
//                如果有提取出来@RequestMapping
                RequestMapping mapping = aClass.getAnnotation(RequestMapping.class);
                //得到@RequestMapping的value,即:/value/value这种连接
                String classPath = mapping.value();
                //取得这个类全部下属方法@RequestMapping在类级上的value加上方法级上@RequestMapping的value才是这个标记了@RequestMapping的方法的key(真正的路径)
                Method[] methods = aClass.getMethods();
                //遍历每个方法,将类上@RequestMapping的value和方法上@RequestMapping的value拼接起来
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        //寻找相应url
                        RequestMapping mapping2 = method.getAnnotation(RequestMapping.class);
                        String methodPath = mapping2.value();
                        String requestPaht = classPath + methodPath;
                        //放入内容
                        handlemap.put(requestPaht, method);
                    }

                }


            }
        }
    }

    /**
     * Spring
     * ID 依赖注入
     */
    public void doAutowired() {
//        map的遍历.
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            //取得bean的实例
            Object instance = entry.getValue();
            //得到bean的类文件
            Class<?> aClass = instance.getClass();
            //是否存在@Controller这个注解.
            if (aClass.isAnnotationPresent(EnjoyController.class)) {
                //遍历其中的成员变量字段
                Field[] fields = aClass.getDeclaredFields();
                //遍历成员变量
                for (Field field : fields) {
                    //确定有@AutoWired字段
                    if (field.isAnnotationPresent(AutoWired.class)) {
//                        得到@AutoWired中的value
                        AutoWired autoWired = field.getAnnotation(AutoWired.class);
                        String key = autoWired.value();
                        System.out.println(key);
//                        因为在doInstance方法中已经将bean加载进了名为beans的HashMap中,这里可以直接通过key获得.
                        Object value = beans.get(key);
                        System.out.println(value);
                        //因为正常来讲我们会将成员变量设为private访问限制付,所以要开启
                        field.setAccessible(true);
                        try {
//                            注入流程 todo 这是什么情况?为啥是set 注入的是field
                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * Spring
     * 创建IOC容器
     */
    public void doInstance() {
        //巡视
        for (String className : classNames) {
            //消灭掉最后的.class后缀名
            System.out.println(className);
            String cn = className.replace(".class", "");//可以用replace进行更换.
            try {
                //找到class,字节码文件?
                Class<?> aClass = Class.forName(cn);
//                确定上面是否有Controller
                if (aClass.isAnnotationPresent(EnjoyController.class)) {
//                    实例化
                    java.lang.Object instance = aClass.newInstance();
//                    寻找上面的@RequestMapping注解(只有带@RequestMapping的类有必要注入)
//                    Controller没有DI的必要,所以应该使用其@RequestMapping注解中的内容作为其key,用来在调用时寻找目标
                    RequestMapping requestMapping = aClass.getAnnotation(RequestMapping.class);
//                    获得@RequestMapping的value值.
                    String key = requestMapping.value();
                    System.out.println("put+"+key);
                    //将value值和这个实例化的类输入到
                    beans.put(key, instance);
                } else if (aClass.isAnnotationPresent(Service.class)) {
                    //逻辑同上
                    java.lang.Object instance = aClass.newInstance();
                    Service service = aClass.getAnnotation(Service.class);
                    String key = service.value();
                    beans.put(key, instance);
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 准备部分:
     * 将全部扫描内容
     * todo 了解一下这是什么东西
     * @param basepackage
     */
    public void basePackageScan(String basepackage) {
        //扫描编译好的类路径--class
        //url=file:/E:/apache-tomcat-8.5.57/webapps/ROOT/WEB-INF/classes/ 类似于这种 todo .getResource是啥意思?
        //replcaeAll里面要匹配.
        URL url = this.getClass().getClassLoader().getResource("/" + basepackage.replaceAll("\\.", "/"));
        System.out.println("zhixing");
//        System.out.println(url);
        assert url != null;
        String fileStr = url.getFile();
//        System.out.println(fileStr);
        File file = new File(fileStr);
        String[] list = file.list();
        for (String path : list) {
            File filePath = new File(fileStr + path);
            //如果是文件夹
            if (filePath.isDirectory()) {
                basePackageScan(basepackage + "." + path);
            } else {
//                System.out.println(basepackage + "." + filePath.getName());
                //不是文件夹
                classNames.add(basepackage + "." + filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    //post方法.


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//得到访问uri
        String uri = req.getRequestURI();
        System.out.println(uri+"1");
        //获得其中除去localhost:8080什么的之类的部分
        String contextPath = req.getContextPath();
        String path = uri.replace(contextPath, "");//消除掉前面那个部分.
        System.out.println(path+"2");
//        通过剩下部分的内容获得方法
        Method method = (Method) handlemap.get(path);
        System.out.println(path.split("/")[1]);
//        得到这个method的类
        MyController controller = (MyController) beans.get("/"+path.split("/")[1]);
        System.out.println(controller+"3");
//调用方法.
        try {
            method.invoke(controller, "yanzezhong","chenggong");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
