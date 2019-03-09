package com.web.dao;

import com.tnsoft.hibernate.model.Role;

import java.util.List;

public interface RoleDAO extends BaseDAO<Role> {

    List<Role> getRoles();

    List<Role> getDomainRolesByAdminRoleIdAndDomainId(int roleId, int domainId);

    List<Role> getRoles(int roleId);

    List<Role> getAdminRoles();

}
