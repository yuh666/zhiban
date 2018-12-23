package test;

import com.daojia.yonhu.classoader.OptClassLoader;

import java.lang.reflect.Method;

public class TestClazzLoader {

    public static void main(String[] args) throws Exception {
        OptClassLoader optClassLoader = new OptClassLoader();
        Class<?> clazz = Class.forName("com.daojia.yonhu.jar.ZhiBanTask", true, optClassLoader);
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("run");
        method.invoke(obj);
    }
}
