package com.web.dao.impl;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.UserDAO;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("userDAO")
public class UserDAOImpl extends BaseDAOImpl<User> implements UserDAO {

    @Override
    public User getUserByName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }
        String hql = "from User where name=?";
        List<User> userList = getByHQL(hql, userName);
        if (!userList.isEmpty()) {
            return userList.get(0);
        }
        return null;
    }

    @Override
    public User getUserByMobile(String mobile) {
        String hql = "from User where mobile=?";
        List<User> userList = getByHQL(hql, mobile);
        if (!userList.isEmpty()) {
            return userList.get(0);
        }
        return null;
    }

    @Override
    public List<Role> getUserRole(int userId) {
        String hql = "select roleId from UserRole where userId=?";
        List<Integer> list = getSession().createQuery(hql).setParameter(0, userId).list();
        List<Role> role = new ArrayList<>();

        for (Integer i : list) {
            role = getSession().createCriteria(Role.class).add(Restrictions.eq("id", i)).list();
        }
        return role;
    }

    @Override
    public List<User> getUsersByAdminId(int adminId) {
        User user = getById(adminId);
        if (null == user) {
            return null;
        }
        String hql = "from User where domainId=? and id<>" + adminId;
        return getByHQL(hql, user.getDomainId());
    }

    @Override
    public List<User> getUsersByDomainId(int domainId) {
        String hql = "from User where domainId=?";
        return getByHQL(hql, domainId);
    }

}
