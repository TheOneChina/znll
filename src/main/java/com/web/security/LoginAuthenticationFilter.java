package com.web.security;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.LoginSessionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private boolean remember;
    private String username;
    private String password;
    private boolean rememberAccount;
    private boolean rememberPassword;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        username = obtainUsername(request).trim();
        password = obtainPassword(request);
        String code = obtainCode(request);
        String r = obtainRemember(request);

        remember = !StringUtils.isEmpty(r);
        this.rememberAccount = !StringUtils.isEmpty(obtainRememberAccount(request));
        this.rememberPassword = !StringUtils.isEmpty(obtainRememberPassword(request));

        LoginAuthenticationToken authRequest = new LoginAuthenticationToken(
                username, password, code);

        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication
            authResult) throws IOException, ServletException {

        LoginUser user = (LoginUser) authResult.getPrincipal();

        DbSession db = BaseHibernateUtils.newSession();
        try {
            LoginSession lg = LoginSessionFactory.getLoginSession(db, user);
            HttpSession session = request.getSession();
            session.setAttribute(ServletConsts.ATTR_USER, lg);
            session.setAttribute("roleId", lg.getDefRole().getId());

            String version = (String) request.getAttribute("version");
            session.setAttribute("version", version);

            addCookie("username", username, this.rememberAccount, response, request);
            addCookie("password", password, this.rememberPassword, response, request);

        } finally {
            db.close();
        }

        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.getSession().setAttribute("ERROR", failed.getMessage());
        }
        super.unsuccessfulAuthentication(request, response, failed);
    }

    protected String obtainCode(HttpServletRequest request) {
        return request.getParameter("code");
    }

    protected String obtainRemember(HttpServletRequest request) {
        return request.getParameter("remember_me");
    }

    protected String obtainRememberAccount(HttpServletRequest request) {
        return request.getParameter("remember-account");
    }

    protected String obtainRememberPassword(HttpServletRequest request) {
        return request.getParameter("remember-pwd");
    }

    private void addCookie(String name, String value, boolean flag, HttpServletResponse response, HttpServletRequest
            request) {
        Cookie cookie = new Cookie(name, value);

        //cookie.setPath(request.getContextPath()+"/");

        if (!flag) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(7 * 24 * 60 * 60);
        }
        response.addCookie(cookie);
    }
    
    /*
    private void addCookie(String name, String password, boolean rememberMe, HttpServletResponse response,
    HttpServletRequest request) throws UnsupportedEncodingException {
        if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(password)){
            //创建Cookie
            Cookie nameCookie=new Cookie("name",name);
            Cookie pswCookie=new Cookie("psw",password);
            
            //设置Cookie的父路径
            //nameCookie.setPath(request.getContextPath()+"/");
            //pswCookie.setPath(request.getContextPath()+"/");
            
            //获取是否保存Cookie
            if(!rememberMe){//不保存Cookie
            Logger.info("不保存Cookie");
                nameCookie.setMaxAge(0);
                pswCookie.setMaxAge(0);
            }else{//保存Cookie的时间长度，单位为秒
            Logger.info("保存Cookie的时间长度，单位为秒");
                nameCookie.setMaxAge(7*24*60*60);
                pswCookie.setMaxAge(7*24*60*60);
            }
            
            Logger.info("加入Cookie到响应头");
            //加入Cookie到响应头
            response.addCookie(nameCookie);
            response.addCookie(pswCookie);
        }
    }
    */
}
