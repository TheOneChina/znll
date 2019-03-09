package com.tnsoft.web.dao;

import com.tnsoft.hibernate.model.UserExpress;

import java.util.List;

public interface UserExpressDAO extends BaseDAO<UserExpress> {

    //找到该条关系，不筛选状态
    UserExpress getLastUserExpress(Integer expressId);

//    List<UserExpress> getUserExpressByUId(int start, int rows, Integer userId);
//
//    List<UserExpress> getUserExpressByUId(Integer userId);
//
//    Integer getCountByUserId(Integer userId);

    void deleteAllByExpressId(int expressId);

    //根据订单id找到该订单所有活动状态的人员订单关系
    List<UserExpress> getUserExpressByEId(Integer expressId);

    List<Integer> getExpressIdListByUserId(int userId, int status);

}
