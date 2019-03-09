package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.UserExpress;

public interface UserExpressService extends BaseService<UserExpress> {

    boolean createUserExpressRelation(Integer userId, Integer expressId);

    boolean setAllActiveUserExpressToFinishedByEId(Integer expressId);

}
