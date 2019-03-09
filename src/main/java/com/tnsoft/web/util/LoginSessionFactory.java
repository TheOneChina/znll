package com.tnsoft.web.util;

import com.tnsoft.hibernate.DbSession;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.security.LoginUser;

import java.security.SecureRandom;

/**
 * 登录信息，登录信息会保存在浏览器cookies里
 */
public final class LoginSessionFactory {

    private LoginSessionFactory() {
    }

    public static LoginSession getLoginSession(DbSession db, LoginUser user) {
        LoginSession loginSession = new LoginSession();
        loginSession.setUserName(user.getUsername());
        loginSession.setUserId(user.getId());
        loginSession.setStatus(user.getStatus());
        loginSession.setNickName(user.getNickName());
        loginSession.setSessionId(new SecureRandom().nextLong());
        loginSession.setDomainId(user.getDomainId());
        loginSession.setRootDomainId(user.getRootDomainId());
        loginSession.setDomain(DBUtils.getDomainByUserId(db, user.getId()));
        loginSession.setRoles(DBUtils.getRoleListByUserId(db, user.getId()));
        loginSession.setDefRole(loginSession.getRoles().get(0));

        return loginSession;
    }

}
