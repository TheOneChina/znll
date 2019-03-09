package com.web.dao.impl;

import com.tnsoft.hibernate.model.RolePermission;
import com.tnsoft.web.dao.RolePermissionDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("rolePermissionDAO")
public class RolePermissionDAOImpl extends BaseDAOImpl<RolePermission> implements RolePermissionDAO {

    @Override
    public List<RolePermission> getRolePermissionByRId(Integer roleId) {
        // TODO Auto-generated method stub
        String hql = "from RolePermission where roleId=? and status=? order by permissionId ";
        List<RolePermission> list = getByHQL(hql, roleId, Constants.State.STATE_ACTIVE);
        return list;
    }

}
