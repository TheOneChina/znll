package com.web.service;

import com.tnsoft.hibernate.model.Role;

import java.util.List;

public interface RoleService extends BaseService<Role> {

    List<Role> getAllRole();

}
