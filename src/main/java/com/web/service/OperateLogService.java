package com.web.service;

import com.tnsoft.hibernate.model.OperateLog;

import java.util.Date;
import java.util.List;

public interface OperateLogService extends BaseService<OperateLog> {

    List<OperateLog> getLogs(int userId, boolean isConsiderDomainRights, Integer offset, Integer limit, Date
            startTime, Date endTime);

}
