package com.tnsoft.web.service;

import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.web.model.Response;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AlertService extends BaseService<NDAAlert> {

    // 获取 -- 当前报警,物流版
//    Object getUnhandledAlerts(LoginSession l);

    //获取最新报警
    NDAAlert getNewestOneAlertByUserId(int userId);

    //获取订单的所有报警
    Response getAllAlertsByExpressId(int expressId);

    //web端获取使用
    Map<String, String> webGetAlertsByExpressId(int expressId);

    // 根据设备编号去查询该设备是否有报警
    List<NDAAlert> getAlertByTagNo(String tagNo);

    //根据用户查询报警信息，医药版和标准版
    Response getAlertMonitors(int userId, boolean isCurrentMonitor, Date startTime, Date endTime, Integer offset,
                              Integer limit);

    //关闭订单所有报警
    Response handleAlertsByExpressId(int expressId);

//    int getHandledAlertsCountByExpressId(int expressId);
//    int getUnhandledAlertsCountByExpressId(int expressId);

}
