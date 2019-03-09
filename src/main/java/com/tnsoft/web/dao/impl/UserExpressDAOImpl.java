package com.tnsoft.web.dao.impl;

import com.tnsoft.hibernate.model.UserExpress;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.model.Constants;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("userExpressDAO")
public class UserExpressDAOImpl extends BaseDAOImpl<UserExpress> implements UserExpressDAO {

    @Override
    public UserExpress getLastUserExpress(Integer expressId) {
        if (null == expressId) {
            return null;
        }
        String hql = "from UserExpress where expressId=? order by id desc";
        List<UserExpress> list = getByHQL(hql, expressId);
        if (null == list || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

//    @Override
//    public Integer getCountByUserId(Integer userId) {
//        // 获得配送员手上所有持有的订单
//        String hql = "from UserExpress where userId=? and status=?";
//        return count(hql, userId, Constants.State.STATE_ACTIVE);
//    }

    @Override
    public void deleteAllByExpressId(int expressId) {
        String hql = "delete from UserExpress where expressId=?";
        Query q = this.getSession().createQuery(hql);
        q.setInteger(0, expressId);
        q.executeUpdate();
    }

//    @Override
//    public List<UserExpress> getUserExpressByUId(int start, int rows, Integer userId) {
//        // 获得配送员手上所有持有的订单
//        String hql = "from UserExpress where userId=? and status=?";
//        return getByHQLWithLimits(start, rows, hql, userId, Constants.State.STATE_ACTIVE);
//    }
//
//    @Override
//    public List<UserExpress> getUserExpressByUId(Integer userId) {
//        // 获得配送员手上所有持有的订单
//        String hql = "from UserExpress where userId=? and status=?";
//        return getByHQL(hql, userId, Constants.State.STATE_ACTIVE);
//    }

    @Override
    public List<UserExpress> getUserExpressByEId(Integer expressId) {
        String hql = "from UserExpress where expressId=? and status=?";
        return getByHQL(hql, expressId, Constants.State.STATE_ACTIVE);
    }

    @Override
    public List<Integer> getExpressIdListByUserId(int userId, int status) {
        String hql = "from UserExpress where userId=? and status=?";
        List<UserExpress> list = getByHQL(hql, userId, status);
        List<Integer> result = new ArrayList<>();
        if (null != list && !list.isEmpty()) {
            for (UserExpress ue : list) {
                result.add(ue.getExpressId());
            }
        }
        return result;
    }

}
