package com.web.security;

import com.expertise.common.logging.Logger;
import com.tnsoft.web.servlet.ServletConsts;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutHandlerr implements LogoutHandler {

    public LogoutHandlerr() {
        super();
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response, Authentication authentication) {
        //clear sessions
        HttpSession session = request.getSession();
        session.removeAttribute(ServletConsts.ATTR_USER);

        Logger.info("Logout! username: " + authentication.getName());
    }

}
