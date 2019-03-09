package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.LoginSession;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.model.Result;

import java.util.List;

public interface UserService extends BaseService<User> {

    Result updateUser(String nickName, int id, int roleId, String staffNo, String gender, String mobile, String
            address, String description);

    Result createUser(String name, String nickName, int roleId, String staffNo, String gender, String mobile,
                      String address, String description, int domainId);

    String savePwd(String id, String oldpwd, String newpwd);

    String resetPwd(String id, String account, String newpwd);

    Response savePwdWithoutOldPwd(int userId, String password);

    List<Role> getUserRole(Integer userId);

    List<Role> getRoles(LoginSession lg);

    List<Role> getRoles(int roleId);

    List<Role> getDomainRolesByAdminRoleIdAndDomainId(Integer roleId, Integer domainId);

    Integer getDomainIdByUserId(Integer userId);

    //获取该用户站点普通用户及下属站点管理员用户
    List<User> getUserListByUserId(int userId);

    User getUSerByUserName(String userName);
}
