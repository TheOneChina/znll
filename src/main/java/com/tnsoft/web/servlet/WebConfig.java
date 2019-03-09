package com.tnsoft.web.servlet;

import java.io.File;
import java.util.Properties;

/**
 * web配置参数，暂时用不到，未来可用于支付参数配置等
 */
public final class WebConfig {

    public static final String PREFIX_USER = "user.";
    public static final String INCOMPATIBLE_VERSIONS = "incompatible_versions";
    public static final String WEB_URL = "web_url";
    public static final String PARTNER = "partner";
    public static final String PRIVATE_KEY = "private_key";
    public static final String SIGN_TYPE = "sign_type";
    public static final String ALI_PUBLIC_LEY = "ali_public_key";

    public static final int THUMBNAIL_WIDTH = 144;
    public static final int THUMBNAIL_HEIGHT = 144;

    private static final WebConfig INSTANCE = new WebConfig();

    private Properties prop;
    private File webBaseDir;

    private WebConfig() {
        prop = new Properties();
    }

    public static WebConfig getInstance() {
        return INSTANCE;
    }

    public void setWebBaseDir(File webBaseDir) {
        this.webBaseDir = webBaseDir;
    }

    public File getWebBaseDir() {
        return webBaseDir;
    }

    public String getWebUrl() {
        return prop.getProperty(WEB_URL, "https://www.tn-soft.com");
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return prop.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }

}
