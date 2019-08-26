package com.lh.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 封装所有公共操作，包括加载配置文件、json操作等
 */
public class CommUtil {
    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        InputStream in = CommUtil.class.getClassLoader()
                .getResourceAsStream(fileName);
        try {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("资源文件加载失败");
            e.printStackTrace();
            return null;
        }
        return properties;
    }

    private static final Gson GSON = new GsonBuilder().create();
    /**
     * 将任意对象转换为字符串
     */
    public static String obj2Json(Object object){
        return GSON.toJson(object);
    }


    public static Object json2obj(String str,Class clz){
        return GSON.fromJson(str,clz);
    }



}
