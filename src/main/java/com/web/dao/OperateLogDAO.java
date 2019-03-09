package com.web.dao;

import com.tnsoft.hibernate.model.OperateLog;

import java.util.List;

public interface OperateLogDAO extends BaseDAO<OperateLog> {

    List<OperateLog> getByDomainId(int domainId);

    List<OperateLog> getByUserId(int userId);
}
