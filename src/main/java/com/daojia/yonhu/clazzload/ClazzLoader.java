package com.daojia.yonhu.clazzload;

import java.io.*;

public class ClazzLoader extends ClassLoader {

//    private static String clazzPath = "/opt/zhiban/clazz/{}.class";
    private static String clazzPath = "/home/lizx/lizx/kaifa/IdeaProjects-mmall/zhiban/src/test/java/test/{}.class";

    @Override
    protected Class<?> findClass(String clazzName) throws ClassNotFoundException {
        File clazzFile = getClazzFile(clazzName.substring(clazzName.lastIndexOf(".")));
        try {
            byte[] clazzBytes = getClazzBytes(clazzFile);
            return this.defineClass(clazzName, clazzBytes, 0, clazzBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.findClass(clazzName);
    }

    private byte[] getClazzBytes(File clazzFile) throws Exception {
        FileInputStream fis = new FileInputStream(clazzFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(fis, baos);
        return baos.toByteArray();
    }

    private void copy(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
    }

    private File getClazzFile(String clazzName) {
        String pathName = String.format(clazzPath, clazzName);
        return new File(pathName);
    }

}
