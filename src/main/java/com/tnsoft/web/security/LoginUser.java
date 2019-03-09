package com.tnsoft.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class LoginUser extends User {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nickName;
    private int status;
    private int domainId;
    private int rootDomainId;

    public LoginUser(int id,
                     String nickName,
                     int status,
                     String username,
                     String password,
                     int domainId,
                     int rootDomainId,
                     final boolean enabled,
                     final boolean accountNonExpired,
                     final boolean credentialsNonExpired,
                     final boolean accountNonLocked,
                     final Collection<? extends GrantedAuthority> authorities) {
        super(username,
                password,
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities);
        this.id = id;
        this.nickName = nickName;
        this.status = status;
        this.domainId = domainId;
        this.rootDomainId = rootDomainId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public int getRootDomainId() {
        return rootDomainId;
    }

    public void setRootDomainId(int rootDomainId) {
        this.rootDomainId = rootDomainId;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
