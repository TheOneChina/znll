package com.tnsoft.web.servlet;

import com.swetake.util.Qrcode;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 二维码生成,开源代码
 */
public class QRCodeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public QRCodeServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");

        try {
            Qrcode testQrcode = new Qrcode();
            testQrcode.setQrcodeErrorCorrect('M');
            testQrcode.setQrcodeEncodeMode('B');
            testQrcode.setQrcodeVersion(7);
            byte[] d = code.getBytes("gbk");
            BufferedImage image = new BufferedImage(98, 98,
                    BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = image.createGraphics();
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, 98, 98);
            g.setColor(Color.BLACK);
            if (d.length > 0 && d.length < 120) {
                boolean[][] s = testQrcode.calQrcode(d);
                for (int i = 0; i < s.length; i++) {
                    for (int j = 0; j < s.length; j++) {
                        if (s[j][i]) {
                            g.fillRect(j * 2 + 3, i * 2 + 3, 2, 2);
                        }
                    }
                }
            }
            g.dispose();
            image.flush();
            ImageIO.write(image, "jpg", response.getOutputStream());
        } finally {
            response.getOutputStream().close();
        }
    }

}
