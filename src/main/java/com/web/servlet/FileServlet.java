package com.web.servlet;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.BinaryFile;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.sql.Blob;

/**
 * 图片下载（预留）
 */
public class FileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DownloadHandler.download(req, resp);
    }

    private static final class DownloadHandler {

        @SuppressWarnings("unused")
        public static void download(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            DbSession db = BaseHibernateUtils.newSession();
            try {
                req.setCharacterEncoding(StringUtils.UTF_8);
                resp.setCharacterEncoding(StringUtils.UTF_8);

                String idStr = req.getParameter("id");

                if (StringUtils.isEmpty(idStr)) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                int id = Integer.parseInt(idStr);
                Integer iconId = id;

                db.beginTransaction();

                if (iconId == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                StringBuilder name = new StringBuilder("filename=");
                name.append("icon");

                BinaryFile icon = (BinaryFile) db.get(BinaryFile.class, iconId);
                if (icon == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                name.append(icon.getId() + ".");
                String fileSuffixName = "jpg";
                resp.setContentType(ServletConsts.CONTENT_TYPE_JPEG);

                OutputStream os = resp.getOutputStream();
                Blob blob = icon.getContent();
                InputStream is = blob.getBinaryStream();

                name.append(fileSuffixName);
                resp.setHeader("Content-Disposition", name.toString());

                IOUtils.copy(is, os);
                is.close();
                os.close();

                db.commit();
                return;
            } catch (NumberFormatException e) {
                if (!resp.isCommitted()) resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SocketException e) {
                if (!resp.isCommitted()) resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                Logger.error(e);
                if (!resp.isCommitted()) resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                Logger.error(e);
                if (!resp.isCommitted()) resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                db.close();
            }
        }

    }

}
