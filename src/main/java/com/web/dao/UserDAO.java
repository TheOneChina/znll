package com.web.dao;

import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;

import java.util.List;

public interface UserDAO extends BaseDAO<User> {

    User getUserByName(String userName);

    User getUserByMobile(String userName);

    List<Role> getUserRole(int UserId);

    List<User> getUsersByAdminId(int adminId);

    List<User> getUsersByDomainId(int domainId);
}
