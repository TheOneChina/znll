package com.tnsoft.web.service.impl;

import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.AlertDAO;
import com.tnsoft.web.dao.ExpressDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.dao.UserExpressDAO;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.AlertService;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.util.Utils;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service("alertService")
public class AlertServiceImpl extends BaseServiceImpl<NDAAlert> implements AlertService {

    @Resource(name = "alertDAO")
    private AlertDAO alertDao;
    @Resource(name = "expressDAO")
    private ExpressDAO expressDAO;
    @Resource(name = "userDAO")
    private UserDAO userDAO;
    @Resource(name = "userExpressDAO")
    private UserExpressDAO userExpressDAO;
    @Resource()
    private ExpressService expressService;

//    @Override
//    public Object getUnhandledAlerts(LoginSession lg) {
//        Map<String, Object> ans = new HashMap<>();
//        List<ExpressWithAlerts> result = new ArrayList<>();
//        DbSession session = BaseHibernateUtils.newSession();
//        try {
//            int roleId = lg.getRoles().get(0).getId();
//            String sql;
//            if (roleId == Constants.Role.SUPER_ADMIN) {
//                sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
//                        + "AND a.express_id IN (SELECT id FROM nda_express WHERE status!=2 and domain_id="
//                        + lg.getDomainId() + ") order by a.creation_time DESC ";
//            } else {
//                sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
//                        + "AND a.express_id IN (SELECT id FROM nda_express WHERE status!=2 and domain_id=" + lg
//                        .getDomainId()
//                        + ") order by a.creation_time DESC ";
//            }
//
//            SQLQuery query = session.createSQLQuery(sql);
//            query.addEntity(NDAAlert.class);
//            // 获取相应的报警信息
//            List<?> list = query.list();
//            if (!list.isEmpty()) {
//                for (Object object : list) {
//                    NDAAlert ndaAlert = (NDAAlert) object;
//                    AlertElem alertElem = new AlertElem();
//
//                    if (ndaAlert.getStatus() == Constants.AlertState.STATE_ACTIVE) {
//                        alertElem.setStatusName("未处理");
//                    } else {
//                        alertElem.setStatusName("已处理");
//                    }
//
//                    alertElem.setTagNo(ndaAlert.getTagNo());
//                    if (ndaAlert.getType() == Constants.AlertType.STATE_TEMPHISALERT) {
//                        if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
//                            alertElem.setAlertName("温度过高");
//                        } else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
//                            alertElem.setAlertName("温度过低");
//                        } else {
//                            alertElem.setAlertName("严重报警");
//                        }
//                    }
//                    if (ndaAlert.getType() == Constants.AlertType.STATE_ELECTRICITY) {
//                        if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
//                            alertElem.setAlertName("电量不足百分之60");
//                        } else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
//                            alertElem.setAlertName("电量不足百分之40");
//                        } else if (ndaAlert.getAlertLevel() == Constants.AlertLevel.STATE_SERIOUS) {
//                            alertElem.setAlertName("电量不足百分之20,请尽快充电");
//                        }
//                    }
//                    alertElem.setTime(Utils.SF.format(ndaAlert.getCreationTime()));
//
//                    ExpressWithAlerts eWithAlerts = null;
//                    for (ExpressWithAlerts eWithAlertsTemp : result) {
//                        if (eWithAlertsTemp.getExpressId() == ndaAlert.getExpressId()) {
//                            eWithAlerts = eWithAlertsTemp;
//                            break;
//                        }
//                    }
//                    if (eWithAlerts == null) {
//                        eWithAlerts = new ExpressWithAlerts();
//                        eWithAlerts.setExpressId(ndaAlert.getExpressId());
//                        eWithAlerts.setDomainId(ndaAlert.getDomainId());
//                        eWithAlerts.updateAlertsShow(
//                                "<table class=\"table table-striped " +
//                                        "table-bordered\"><tr><td>报警时间</td><td>报警类型</td><td>状态</td><td>模块编号</td
// ></tr>");
//                        Express express = DBUtils.getNDAExpress(session, ndaAlert.getExpressId());
//                        if (express != null) {
//                            eWithAlerts.setExpressNo(express.getExpressNo());
//                        }
//
//                        UserExpress ue = DBUtils.getUserByExpressId(session, ndaAlert.getExpressId());
//                        if (ue != null) {
//                            User user = (User) session.get(User.class, ue.getUserId());
//                            if (user != null) {
//                                eWithAlerts.setUserName(user.getNickName());
//                                eWithAlerts.setMobile(user.getMobile());
//                            }
//                        }
//                        result.add(eWithAlerts);
//                    }
//                    eWithAlerts.addAlertElem(alertElem);
//                    StringBuilder sb = new StringBuilder("");
//                    sb.append("<tr><td>").append(alertElem.getTime());
//                    sb.append("</td><td>").append(alertElem.getAlertName());
//                    sb.append("</td><td>").append(alertElem.getStatusName());
//                    sb.append("</td><td>").append(alertElem.getTagNo()).append("</td></tr>");
//                    eWithAlerts.updateAlertsShow(sb.toString());
//                }
//
//                for (int i = 0; i < result.size(); i++) {
//                    result.get(i).updateAlertsShow("</table>");
//                }
//            }
//        } finally {
//            session.close();
//        }
//        ans.put("data", result);
//        return ans;
//    }

    @Override
    public NDAAlert getNewestOneAlertByUserId(int userId) {

        User user = userDAO.getById(userId);
        if (null == user) {
            return null;
        }
        List<Role> roles = userDAO.getUserRole(userId);
        if (null == roles || roles.isEmpty()) {
            return null;
        }
        if (roles.get(0).isDomainRights()) {
            //具有站点权限，一般为管理员
            NDAAlert alert = alertDao.getNewestOneByDomainId(user.getDomainId());
            if (null != alert) {
                Express express = expressDAO.getById(alert.getExpressId());
                if (null != express) {
                    alert.setExpress(express.getExpressNo());
                }
            }
            return alert;

        } else {

            List<Integer> expressIds = userExpressDAO.getExpressIdListByUserId(userId, Constants
                    .UserExpressState.STATE_ACTIVE);

            if (null != expressIds && !expressIds.isEmpty()) {
                List<NDAAlert> alertList = new ArrayList<>();
                for (int id : expressIds) {
                    NDAAlert alertTemp = alertDao.getNewestOneByExpressId(id);
                    if (null != alertTemp) {
                        alertList.add(alertTemp);
                    }
                }
                int idx = 0;
                for (int i = 0; i < alertList.size(); i++) {
                    if (alertList.get(idx).getId() < alertList.get(i).getId()) {
                        idx = i;
                    }
                }
                Express express = expressDAO.getById(alertList.get(idx).getExpressId());
                alertList.get(idx).setExpress(express.getExpressNo());
                return alertList.get(idx);
            }
        }
        return null;
    }

    @Override
    public Response getAllAlertsByExpressId(int expressId) {
        Response response = new Response(Response.ERROR);
        if (expressId < 1) {
            return response;
        }
        List<NDAAlert> list = alertDao.getAlertsByExpressId(expressId);
        if (null != list && !list.isEmpty()) {
            response.setNdaAlerts(list);
            response.setCode(Response.OK);
        }
        return response;
    }

    @Override
    public Map<String, String> webGetAlertsByExpressId(int expressId) {
        Response response = getAllAlertsByExpressId(expressId);
        Map<String, String> ans = new HashMap<>();
        if (response.getCode() == Response.OK) {
            List<NDAAlert> ndaAlerts = response.getNdaAlerts();
            if (null != ndaAlerts && ndaAlerts.size() > 0) {
                StrBuilder sb = new StrBuilder();
                for (int i = 0; i < ndaAlerts.size(); i++) {
                    NDAAlert alert = ndaAlerts.get(i);
                    if (null != alert.getAlertTime()) {
                        sb.append("<tr><td>").append(Utils.SF.format(alert.getAlertTime())).append("</td>");
                    } else {
                        sb.append("<tr><td></td>");
                    }

                    if (alert.getType() == Constants.AlertType.STATE_TEMPHISALERT) {
                        if (alert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_HIGH) {
                            sb.append("<td>温度过高</td>");
                        } else if (alert.getAlertLevel() == Constants.AlertLevel.STATE_NORAML_LOW) {
                            sb.append("<td>温度过低</td>");
                        } else {
                            sb.append("<td>严重报警</td>");
                        }
                    } else if (alert.getType() == Constants.AlertType.STATE_ELECTRICITY) {
                        sb.append("<td>电量过低</td>");
                    } else if (alert.getType() == Constants.AlertType.STATE_NOT_RESPONSE) {
                        sb.append("<td>失联报警</td>");
                    }

                    if (alert.getStatus() == Constants.AlertState.STATE_ACTIVE) {
                        sb.append("<td>未处理</td>");
                    } else {
                        sb.append("<td>已处理</td>");
                    }

                    sb.append("<td>").append(Utils.SF.format(alert.getCreationTime())).append("</td></tr>");
                }
                sb.append("</table>");
                ans.put("data", sb.toString());
            }
            return ans;
        } else {
            return ans;
        }
    }

    @Override
    public List<NDAAlert> getAlertByTagNo(String tagNo) {
        return alertDao.getAlertByTagNo(tagNo);
    }

    @Override
    public Response getAlertMonitors(int userId, boolean isCurrentMonitor, Date startTime, Date endTime, Integer
            offset, Integer limit) {
        Response response = new Response(Response.ERROR);
        User user = userDAO.getById(userId);
        if (null == user) {
            return response;
        }
        List<Role> roles = userDAO.getUserRole(userId);
        List<Express> expresses;
        if (null == roles || roles.isEmpty()) {
            return response;
        }
        Role role = roles.get(0);
        if (role.isDomainRights()) {
            //获得该用户站点及其子孙站点的订单
            expresses = expressService.getExpressesByDomainId(user.getDomainId(), isCurrentMonitor);
        } else {
            if (isCurrentMonitor) {
                //用户的当前订单
                List<Integer> expressIdList = userExpressDAO.getExpressIdListByUserId(userId, Constants
                        .UserExpressState.STATE_ACTIVE);
                expresses = new ArrayList<>();
                if (!expressIdList.isEmpty()) {
                    for (Integer i : expressIdList) {
                        expresses.add(expressDAO.getById(i));
                    }
                }
            } else {
                //用户的历史订单
                List<Integer> expressIdList = userExpressDAO.getExpressIdListByUserId(userId, Constants
                        .UserExpressState.STATE_FINISHED);
                expresses = new ArrayList<>();
                if (!expressIdList.isEmpty()) {
                    for (Integer i : expressIdList) {
                        expresses.add(expressDAO.getById(i));
                    }
                }
            }
        }
        if (null != expresses && expresses.size() > 0) {

            List<Express> expressTemp = new ArrayList<>();

            if (isCurrentMonitor) {
                for (Express express : expresses) {
                    //过滤掉无报警的订单
                    if (express.getAlertCount() + express.getHistoryAlertCount() < 1) {
                        continue;
                    }
                    if (null != express.getCreationTime()) {
                        if (null != startTime && express.getCreationTime().before(startTime)) {
                            continue;
                        }
                        if (null != endTime && express.getCreationTime().after(endTime)) {
                            continue;
                        }
                    }
                    expressTemp.add(express);
                }
            } else {
                for (Express express : expresses) {
                    if (express.getAlertCount() + express.getHistoryAlertCount() < 1) {
                        continue;
                    }
                    if (null != express.getCheckOutTime()) {
                        if (null != startTime && express.getCheckOutTime().before(startTime)) {
                            continue;
                        }
                        if (null != endTime && express.getCheckOutTime().after(endTime)) {
                            continue;
                        }
                    }
                    expressTemp.add(express);
                }
            }
            expresses = expressTemp;

            //排序
            Collections.sort(expresses, new Comparator<Express>() {
                @Override
                public int compare(Express o1, Express o2) {
                    if (null != o1.getCheckOutTime() && null != o2.getCheckOutTime()) {
                        if (o1.getCheckOutTime().after(o2.getCheckOutTime())) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else if (null == o1.getCheckOutTime() && null == o2.getCheckOutTime()) {
                        if (o1.getCreationTime().after(o2.getCreationTime())) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else if (null == o1.getCheckOutTime()) {
                        return -1;
                    } else if (null == o2.getCheckOutTime()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

        }


        if (null != expresses && null != offset && null != limit) {
            if (offset * limit > expresses.size()) {
                expresses = null;
            } else {
                int end;
                if ((offset + 1) * limit > expresses.size()) {
                    end = expresses.size();
                } else {
                    end = (offset + 1) * limit;
                }
                expresses = expresses.subList(offset * limit, end);
            }
        }

//        List<Express> monitors = expresses;

//        if (null != monitors && !monitors.isEmpty()) {
//            List<Express> list = new ArrayList<>();
//            for (int i = 0; i < monitors.size(); i++) {
//                int id = monitors.get(i).getId();
//                List<NDAAlert> alerts = alertDao.getAlertsByExpressId(id, Constants.AlertState.STATE_ACTIVE);
//                int active = 0;
//                if (null != alerts) {
//                    active = alerts.size();
//                }
//                alerts = alertDao.getAlertsByExpressId(id, Constants.AlertState.STATE_FINISHED);
//                int finished = 0;
//                if (null != alerts) {
//                    finished = alerts.size();
//                }
//                if (active + finished > 0) {
//                    monitors.get(i).setAlertCount(active);
//                    monitors.get(i).setHistoryAlertCount(finished);
//                    list.add(monitors.get(i));
//                    response.setAlertCount(response.getAlertCount() + active + finished);
//                }
//            }
//            monitors = list;
//        }

        if (null != expresses) {
            response.setExpress(expresses);
        }
        response.setCode(Response.OK);

        return response;
    }

    @Override
    @Transactional
    public Response handleAlertsByExpressId(int expressId) {
        Response response = new Response(Response.OK);
        List<NDAAlert> alerts = alertDao.getAlertsByExpressId(expressId, Constants.AlertState.STATE_ACTIVE);
        if (null == alerts || alerts.isEmpty()) {
            return response;
        }
        Express express = expressDAO.getById(expressId);
        for (NDAAlert alert : alerts) {
            alert.setStatus(Constants.AlertState.STATE_FINISHED);
            alert.setLastModitied(new Date());
        }
        express.setHistoryAlertCount(express.getHistoryAlertCount() + alerts.size());
        express.setAlertCount(0);
        response.setMessage("报警关闭成功");
        return response;
    }

//    @Override
//    public int getHandledAlertsCountByExpressId(int expressId) {
//        return alertDao.countAlerts(expressId, Constants.AlertState.STATE_ACTIVE);
//    }
//
//    @Override
//    public int getUnhandledAlertsCountByExpressId(int expressId) {
//        return alertDao.countAlerts(expressId, Constants.AlertState.STATE_FINISHED);
//    }
}
