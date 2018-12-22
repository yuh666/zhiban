package test;

import com.daojia.yonhu.clazzload.ClazzLoader;

import java.lang.reflect.Method;

public class TestClazzLoader {

    public static void main(String[] args) throws Exception {
        ClazzLoader clazzLoader = new ClazzLoader();
        Class<?> clazz = Class.forName("com.daojia.yonhu.jar.ZhiBanTask", true, clazzLoader);
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("run");
        method.invoke(obj);
    }
}
