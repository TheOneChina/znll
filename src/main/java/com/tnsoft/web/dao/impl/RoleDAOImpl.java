package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.model.Role;
import com.tnsoft.web.dao.RoleDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.util.ConfigUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("roleDAO")
public class RoleDAOImpl extends BaseDAOImpl<Role> implements RoleDAO {

    @Override
    public List<Role> getRoles() {
        String hql = "from Role where status=? and id <> 1";
        return getByHQL(hql, Constants.State.STATE_ACTIVE);
    }

    @Override
    public List<Role> getDomainRolesByAdminRoleIdAndDomainId(int roleId, int domainId) {
        List<Role> roleList = new ArrayList<>();
        if (roleId == Constants.Role.ADMIN) {
            //物流版可增加次级管理员
            roleList.add(getById(2));
            roleList.add(getById(3));
            roleList.add(getById(4));
            roleList.add(getById(5));
        } else if (roleId == Constants.Role.ADMIN_MEDICINE || roleId == Constants.Role.SUB_ADMIN_MEDICINE) {
            roleList.add(getById(10));
            roleList.add(getById(8));
        } else if (roleId == Constants.Role.ADMIN_STANDARD) {
            roleList.add(getById(9));
        }
        if (domainId > 1) {
            String hql = "from Role where domainId=? and status=?";
            List<Role> list = getByHQL(hql, domainId, Constants.State.STATE_ACTIVE);
            if (null != list && !list.isEmpty()) {
                roleList.addAll(list);
            }
        }
        return roleList;
    }

    @Override
    public List<Role> getRoles(int roleId) {
        String value = ConfigUtils.getStringValue(roleId + "");
        String hql = "from Role where status=? and id in (" + value + ")";
        return getByHQL(hql, Constants.State.STATE_ACTIVE);
    }

    @Override
    public List<Role> getAdminRoles() {
        //获取普通管理员在添加员工时,能为员工选择的角色
        String hql = "from Role where status=? and id <> 1  and id <> 2";
        return getByHQL(hql, Constants.State.STATE_ACTIVE);
    }


}
