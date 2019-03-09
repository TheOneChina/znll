package com.web.service;

import com.tnsoft.hibernate.model.Permission;

import java.util.List;

public interface PermissionService extends BaseService<Permission> {

    List<List<Permission>> getPermission(Integer roleId);

}
