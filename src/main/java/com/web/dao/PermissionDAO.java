package com.web.dao;

import com.tnsoft.hibernate.model.Permission;

import java.util.List;

public interface PermissionDAO extends BaseDAO<Permission> {

    List<Permission> findAll();

    List<Permission> findByUserId(int userId);

}
