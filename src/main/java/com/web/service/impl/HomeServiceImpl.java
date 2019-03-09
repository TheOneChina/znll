package com.web.service.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.web.dao.DomainDAO;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.HomeService;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

@Service("homeService")
public class HomeServiceImpl implements HomeService {

    @Resource()
    private DomainDAO domainDAO;

    @Override
    public Response fileUpload(MultipartFile[] files, HttpServletRequest req) {
        // TODO Auto-generated method stub
        Response res = new Response();
        try {
            // 如果只是上传一个文件，则只需要MultipartFile类型接收文件即可，而且无需显式指定@RequestParam注解
            // 如果想上传多个文件，那么这里就要用MultipartFile[]类型来接收文件，并且还要指定@RequestParam注解
            // 并且上传多个文件时，前台表单中的所有<input
            // type="file"/>的name都应该是myfiles，否则参数里的myfiles无法获取到所有上传的文件
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    System.out.println("文件未上传");
                } else {
                    // 如果用的是Tomcat服务器，则文件会上传到\\%TOMCAT_HOME%\\webapps\\YourWebProject\\WEB-INF\\upload\\文件夹中
                    String realPath = req.getSession().getServletContext().getRealPath("/img");
                    // 这里不必处理IO流关闭的问题，因为FileUtils.copyInputStreamToFile()方法内部会自动把用到的IO流关掉，我是看它的源码才知道的
                    File file1 = new File(realPath, file.getOriginalFilename());
                    System.out.println(file1.getName());
                    FileUtils.copyInputStreamToFile(file.getInputStream(), file1);
                }
            }
            res.setCode(0);
            res.setMessage("替换成功");
        } catch (Exception e) {
            res.setCode(1);
            res.setMessage("替换失败");
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Response savePreferences(int domainId, String preferences) {
        Response response = new Response(Response.ERROR);
        Domain domain = domainDAO.getById(domainId);
        if (StringUtils.isEmpty(preferences)) {
            response.setMessage("请输入内容！");
            return response;
        }
        if (null == domain) {
            response.setMessage("站点不存在！");
            return response;
        }
        domain.setPreferences(preferences);
        domainDAO.update(domain);
        response.setCode(Response.OK);
        response.setMessage("成功！");
        return response;
    }

}
