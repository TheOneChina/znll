package com.web.servlet;

import com.expertise.common.io.IoHelper;
import com.expertise.common.io.file.FileFilter;
import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.web.controller.ProtocolController;
import com.tnsoft.web.model.Response;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * 检测软件更新
 */
public class UpgradeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        FileInputStream is = null;
        try {
            req.setCharacterEncoding(StringUtils.UTF_8);
            String appVersion = req.getParameter("app_version");
            //tag 标识物流、医药与标准版本
            String tag = req.getParameter("tag");
            //Logger.error("app version: " + appVersion);

            //获取app提交的版本信息
            if (appVersion == null) {
                appVersion = req.getParameter("v");
                if (appVersion == null) {
                    appVersion = "0";
                }
            }

            //更新包放在apks目录下
            WebConfig config = WebConfig.getInstance();
            File baseDir = config.getWebBaseDir();
            File apkDir = new File(baseDir.getPath(), "apks"); //$NON-NLS-1$

            //遍历apks目录下文件，根据文件名（xxx.1.2.3.apk）将1。2.3解析为123，和客户端
            //上传的版本号进行匹配，判断是否需要更新
            if (apkDir.exists()) {
                Pattern pattern;
                switch (tag) {
                    case "1":
                        pattern = Pattern.compile("nda" + "([\\d\\.]+)\\.apk");
                        break;
                    case "2":
                        pattern = Pattern.compile("online" + "([\\d\\.]+)\\.apk");
                        break;
                    case "3":
                        pattern = Pattern.compile("yun" + "([\\d\\.]+)\\.apk");
                        break;
                    default:
                        pattern = null;
                }
                FileFilter filter = new FileFilter(pattern, FileFilter.FILES_ONLY, false);
                File[] files = apkDir.listFiles(filter);
                File apk = null;
                int version = Integer.parseInt(appVersion.replaceAll("\\.", ""));
                for (File file : files) {
                    String fileName = file.getName();
                    Matcher m = pattern.matcher(fileName);
                    if (m.matches()) {
                        int newVersion = Integer.parseInt(m.group(1).replaceAll("\\.", ""));

                        if (newVersion > version) {
                            version = newVersion;
                            apk = file;
                        }
                    }
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);

                //设置接口返回数据
                String json = null;
                if (apk != null) {

                    Response resps = new Response(Response.OK);
                    resps.setMessage(apk.getName());
                    json = ProtocolController.GSON.toJson(resps);
                } else {
                    Response resps = new Response(Response.ERROR);
                    json = ProtocolController.GSON.toJson(resps);
                }
                if (json != null) {

                    String acceptEncoding = req.getHeader("Accept-Encoding");
                    if (acceptEncoding != null && acceptEncoding.toLowerCase().contains("gzip")) {
                        resp.setHeader("Content-Encoding", "gzip");
                        GZIPOutputStream os = new GZIPOutputStream(resp.getOutputStream());
                        os.write(StringUtils.toBytesQuietly(json));
                        os.close();
                    } else {
                        resp.getWriter().print(json);
                    }
                    return;
                }
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (RuntimeException e) {
            Logger.error(e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            IoHelper.closeQuietly(is);
        }
    }
}
