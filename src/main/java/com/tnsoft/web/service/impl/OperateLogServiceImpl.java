package com.tnsoft.web.service.impl;

import com.tnsoft.hibernate.model.OperateLog;
import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.OperateLogDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.service.OperateLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("operateLogService")
public class OperateLogServiceImpl extends BaseServiceImpl<OperateLog> implements OperateLogService {

    @Resource()
    private UserDAO userDAO;
    @Resource()
    private OperateLogDAO operateLogDAO;

    @Override
    public List<OperateLog> getLogs(int userId, boolean isConsiderDomainRights, Integer offset, Integer limit, Date
            startTime, Date endTime) {
        User user = userDAO.getById(userId);
        if (null == user) {
            return null;
        }
        List<Role> roles = userDAO.getUserRole(userId);
        List<OperateLog> logs;
        if (null != roles && !roles.isEmpty()) {
            Role role = roles.get(0);
            if (isConsiderDomainRights && role.isDomainRights()) {
                logs = operateLogDAO.getByDomainId(user.getDomainId());
            } else {
                logs = operateLogDAO.getByUserId(userId);
            }
            if (null != logs && logs.size() > 0) {

                List<OperateLog> logsTemp = new ArrayList<>();
                for (OperateLog log : logs) {
                    if (null != log.getOperationTime()) {
                        if (null != startTime && log.getOperationTime().before(startTime)) {
                            continue;
                        }
                        if (null != endTime && log.getOperationTime().after(endTime)) {
                            continue;
                        }
                    }
                    logsTemp.add(log);
                }
                logs = logsTemp;

                if (null != offset && null != limit) {
                    if (offset * limit >= logs.size()) {
                        return null;
                    } else {
                        int endIdx;
                        if ((offset + 1) * limit >= logs.size()) {
                            endIdx = logs.size();
                        } else {
                            endIdx = (offset + 1) * limit;
                        }
                        logs = logs.subList(offset * limit, endIdx);
                    }
                }

            }
            return logs;
        }
        return null;
    }
}
