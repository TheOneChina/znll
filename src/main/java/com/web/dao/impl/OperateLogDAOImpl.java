package com.web.dao.impl;

import com.tnsoft.hibernate.model.OperateLog;
import com.tnsoft.web.dao.OperateLogDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("operateLogDAO")
public class OperateLogDAOImpl extends BaseDAOImpl<OperateLog> implements OperateLogDAO {
    @Override
    public List<OperateLog> getByDomainId(int domainId) {
        String hql = "from OperateLog where domainId=? order by id desc";
        return getByHQL(hql, domainId);
    }

    @Override
    public List<OperateLog> getByUserId(int userId) {
        String hql = "from OperateLog where userId=? order by id desc";
        return getByHQL(hql, userId);
    }
}
