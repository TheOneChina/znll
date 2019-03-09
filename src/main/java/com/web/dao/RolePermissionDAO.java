package com.web.dao;

import com.tnsoft.hibernate.model.RolePermission;

import java.util.List;

public interface RolePermissionDAO extends BaseDAO<RolePermission> {

    List<RolePermission> getRolePermissionByRId(Integer roleId);

}
