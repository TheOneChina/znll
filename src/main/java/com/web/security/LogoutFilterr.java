package com.web.security;

import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

public class LogoutFilterr extends LogoutFilter {


    public LogoutFilterr(String logoutSuccessUrl, LogoutHandler[] handlers) {
        super(logoutSuccessUrl, handlers);
    }

    public LogoutFilterr(LogoutSuccessHandler logoutSuccessHandler,
                         LogoutHandler[] handlers) {
        super(logoutSuccessHandler, handlers);
    }
}
