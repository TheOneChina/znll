package com.tnsoft.web.servlet;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketException;

/**
 * 导出bin文件
 */
public class ExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final int TYPE_BIN = 1;

    public ExportServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding(StringUtils.UTF_8);
        resp.setCharacterEncoding(StringUtils.UTF_8);
        try {
            resp.setContentType(ServletConsts.CONTENT_TYPE_PLAIN);

            //获取导出数据的类型
            int type = Integer.parseInt(req.getParameter("type"));
            switch (type) {
                case TYPE_BIN:
                    resp.setHeader("Content-disposition", "attachment; filename=" + req.getParameter("code") + ".bin");
                    exportBin(resp, req);
                    break;

                default:
                    resp.getWriter().print("您没有访问权限");
                    break;
            }
        } catch (SocketException e) {
            resp.getWriter().print("未知错误");
        } catch (RuntimeException e) {
            Logger.error(e);
            resp.getWriter().print("未知错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    }

    //生成智能标签bin文件内容
    private void exportBin(HttpServletResponse resp, HttpServletRequest req) throws IOException {

        String tmp = req.getParameter("code");
        byte[] arr = new byte[128];
        byte[] t = StringUtils.toBytesQuietly(tmp);
        System.arraycopy(t, 0, arr, 0, t.length);
        ServletOutputStream out = null;
        try {
            out = resp.getOutputStream();
            out.write(arr, 0, arr.length);
        } finally {
            out.close();
        }
    }
}
