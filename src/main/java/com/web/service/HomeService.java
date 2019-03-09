package com.web.service;

import com.tnsoft.web.model.Response;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface HomeService {

    Response fileUpload(MultipartFile[] files, HttpServletRequest req);

    Response savePreferences(int domainId, String preferences);

}
