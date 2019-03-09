package com.web.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class LoginAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private String code;

    public LoginAuthenticationToken(String principal, String credentials, String code) {
        super(principal, credentials);

        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
