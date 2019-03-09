package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.UserRole;

import java.util.List;

public interface UserRoleDAO extends BaseDAO<UserRole> {

    void updateUserRole(int userId, int roleId);

    void saveUserRole(int userId, int roleId);

    UserRole getRoleByUId(Integer userId);

    List<UserRole> getAllAdmin();
}
