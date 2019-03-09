package com.tnsoft.web.model;

import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.hibernate.model.Role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class LoginSession implements Serializable {

    private static final long serialVersionUID = 1L;

    // 用户名
    private String userName;
    private String nickName;
    private int status;
    // 用户id
    private int userId;
    // sessionId
    private long sessionId;
    private String ticket;
    private String preferences;
    private int domainId;
    private int rootDomainId;

    private Domain domain;

    // 用户角色列表
    private List<Role> roles = new ArrayList<>();
    // 用户默认角色
    private Role defRole;

    public LoginSession() {

    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getPreferences() {
        return preferences;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setDefRole(Role defRole) {
        this.defRole = defRole;
    }

    public Role getDefRole() {
        return defRole;
    }

    public int getDomainId() {
        return domainId;
    }

    public void setDomainId(int domainId) {
        this.domainId = domainId;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public int getRootDomainId() {
        return rootDomainId;
    }

    public void setRootDomainId(int rootDomainId) {
        this.rootDomainId = rootDomainId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
