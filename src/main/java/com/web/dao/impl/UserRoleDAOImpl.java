package com.web.dao.impl;

import com.tnsoft.hibernate.model.UserRole;
import com.tnsoft.web.dao.UserRoleDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userRoleDAO")
public class UserRoleDAOImpl extends BaseDAOImpl<UserRole> implements UserRoleDAO {

    @Override
    public void updateUserRole(int userId, int roleId) {
        if (roleId < 2) {
            return;
        }
        UserRole ur = getRoleByUId(userId);
        if (null != ur) {
            ur.setRoleId(roleId);
        }
    }

    @Override
    public UserRole getRoleByUId(Integer userId) {
        String hql = "from UserRole where userId=? and status=" + Constants.UserRoleState.STATE_NORMAL;
        return getOneByHQL(hql, userId);
    }

    @Override
    public List<UserRole> getAllAdmin() {
        String hql = "from UserRole where status=" + Constants.UserRoleState.STATE_NORMAL + "and (roleId=? or roleId=?)";
        return getByHQL(hql, Constants.Role.ADMIN, Constants.Role.SUB_ADMIN_MEDICINE);
    }

    @Override
    public void saveUserRole(int userId, int roleId) {
        if (userId < 1 || roleId < 2) {
            return;
        }
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setFlag(1);
        ur.setStatus(Constants.UserRoleState.STATE_NORMAL);
        save(ur);
    }

}
