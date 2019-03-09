package com.web.servlet;

import com.expertise.common.io.IoHelper;
import com.expertise.common.logging.Logger;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.web.server.NdaServer;
import com.tnsoft.web.service.TagServices;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 容器运行后，进行初始化
 */
public class ContextListenerr implements ServletContextListener {

    private static final String DEFAULT_LOG_NAME = "org.ciotc.web";
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(6);
    private static final int TAG_NO_RESPONSE_CHECK_DELAY_MINUTES = 20;
    private static final int TAG_AVAILABLE_CHECK_DELAY_HOURS = 24;

    private NdaServer ndaServer;

    public ContextListenerr() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void contextInitialized(ServletContextEvent sce) {
        Logger.setDefaultLogger(DEFAULT_LOG_NAME);

        //读取配置文件(预留)
        FileInputStream fis = null;
        try {
            ServletContext context = sce.getServletContext();
            File realPath = new File(context.getRealPath(""));
            File baseDir = new File(realPath.getParentFile().getParentFile(), "conf");

            Properties prop = new Properties();
            File file = new File(baseDir, "admin.properties");
            if (file.exists()) {
                fis = new FileInputStream(new File(baseDir, "admin.properties"));
                prop.load(fis);
            }

            Enumeration<String> en = context.getInitParameterNames();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                if (!prop.contains(key)) {
                    // provide default values
                    prop.setProperty(key, context.getInitParameter(key));
                }
            }

            //自动更新数据库
            Properties hibernateProp = new Properties();
            WebConfig webConfig = WebConfig.getInstance();
            webConfig.setWebBaseDir(realPath);
            Map<String, String> replicationConfig = new HashMap<>();
            for (String key : prop.stringPropertyNames()) {
                if (key.startsWith("hibernate.")) {
                    hibernateProp.put(key.substring(10), prop.getProperty(key));
                } else if (key.startsWith("replication.")) {
                    replicationConfig.put(key.substring(12), prop.getProperty(key));
                } else {
                    webConfig.setProperty(key, prop.getProperty(key));
                }
            }

            BaseHibernateUtils.bootstrap(hibernateProp);

            //运行定时脚本
            TagServices pushService = new TagServices();
            scheduler.scheduleWithFixedDelay(pushService.getTagNotResponseService(), TAG_NO_RESPONSE_CHECK_DELAY_MINUTES, TAG_NO_RESPONSE_CHECK_DELAY_MINUTES, TimeUnit.MINUTES);
            scheduler.scheduleAtFixedRate(pushService.getTagAvailableService(), 10, TAG_AVAILABLE_CHECK_DELAY_HOURS, TimeUnit.HOURS);

            ndaServer = new NdaServer(8000);
            ndaServer.start();

            Logger.info("web server started");
        } catch (Throwable e) {
            Logger.error(e);
            IoHelper.closeQuietly(fis);
            if (ndaServer != null) {
                ndaServer.stop();
            }
            System.exit(-1);
        } finally {
            IoHelper.closeQuietly(fis);
        }
    }

    /**
     * 应用关闭，关闭硬件服务器
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        scheduler.shutdown();
        if (ndaServer != null) {
            ndaServer.stop();
        }
    }
}

