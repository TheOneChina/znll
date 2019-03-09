package com.tnsoft.web.util;

import java.util.Properties;

public class ConfigUtils {

    private static Properties props = new Properties();

    static {
        try {
            props.load(ConfigUtils.class.getClassLoader().getResourceAsStream("role.properties"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getStringValue(String key) {
        String str;
        try {
            str = props.getProperty(key);
        } catch (Exception ex) {
            ex.fillInStackTrace();
            str = "";
        }
        return str;
    }

}
