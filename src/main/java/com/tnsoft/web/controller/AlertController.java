package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.AlertLevel;
import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.NDAAlert;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.AlertService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

@Controller
public class AlertController extends BaseController {

    @Resource(name = "alertService")
    private AlertService alertService;

    public AlertController() {
        super();
    }

    @RequestMapping("/historyAlerts/{flag}")
    public String alertsAll(Model model, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "查看历史报警记录");
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "alert.express.alertHistory";
                break;
            case "2":
                view = "alert.medicine.alertHistory";
                break;
            case "3":
                view = "alert.standard.alertHistory";
                break;
            default:
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return view;
    }

    @RequestMapping("/nowAlerts/{flag}")
    public String alertsNow(Model model, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "查看当前报警列表");
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "alert.express.alertsNow";
                break;
            case "2":
                view = "alert.medicine.alertsNow";
                break;
            case "3":
                view = "alert.standard.alertsNow";
                break;
            default:
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return view;
    }


    @RequestMapping("/getWebAlertMsg")
    @ResponseBody
    public NDAAlert getWebAlertMsg() {
        if (!validateUser()) {
            return null;
        }
        NDAAlert alert = alertService.getNewestOneAlertByUserId(lg.getUserId());
        if (null == alert) {
            return null;
        }
        Date date = new Date();
        //1分钟前的数据剔除
        date.setTime(date.getTime() - 60000L);
        if (null == alert.getAlertTime() || alert.getAlertTime().before(date)) {
            return null;
        } else {
            return alert;
        }
    }

//    @RequestMapping("/getUnhandledAlerts/{flag}")
//    @ResponseBody
//    public Object getUnhandledAlerts(@PathVariable String flag) {
//        if (!validateUser()) {
//            return "";
//        }
//
//        Response res = alertService.getAlertMonitors(lg.getUserId(), true);
//        if (res.getCode() == Response.OK) {
//            Map<String, Object> ans = new HashMap<>();
//            ans.put("data", res.getExpress());
//            return ans;
//        } else {
//            return "";
//        }
//
//    }
//
//    @RequestMapping("/getHistoryAlerts/{flag}")
//    @ResponseBody
//    public Object getHistoryAlerts(@PathVariable String flag) {
//        if (!validateUser()) {
//            return "";
//        }
//        Response res = alertService.getAlertMonitors(lg.getUserId(), false);
//        if (res.getCode() == Response.OK) {
//            Map<String, Object> ans = new HashMap<>();
//            ans.put("data", res.getExpress());
//            return ans;
//        } else {
//            return "";
//        }
//
//    }

    @RequestMapping("/ajaxAlertExpress")
    @ResponseBody
    public Object ajaxAlertExpress(int start, int length, String startTime, String endTime, boolean isCurrent) throws
            UnsupportedEncodingException {
        if (!validateUser()) {
            return "";
        }
        Map<String, Object> result = new HashMap<>();

        Date startDate = null;
        Date endDate = null;
        if (!StringUtils.isEmpty(startTime)) {
            try {
                startDate = Utils.SF.parse(startTime);
            } catch (ParseException e) {
                e.printStackTrace();
                startDate = null;
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            try {
                endDate = Utils.SF.parse(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
                endDate = null;
            }
        }

        Response res = alertService.getAlertMonitors(lg.getUserId(), isCurrent, startDate, endDate, null, null);

        if (res.getCode() == Response.OK) {
            List<Express> expressList = res.getExpress();

            if (null == expressList || expressList.size() < 1) {
                return DBUtils.getEmpty();
            }
            //查询到的数据总条数
            result.put("recordsTotal", expressList.size());

            Map<String, String[]> properties = request.getParameterMap();
            String search = "";
            if (null != properties.get("search[value]")) {
                search = new String(properties.get("search[value]")[0].getBytes("ISO-8859-1"), "UTF-8");
            }
            if (search.contains("'")) {
                search = "";
            }

            if (!StringUtils.isEmpty(search)) {
                List<Express> expresses = new ArrayList<>();
                for (Express express : expressList) {
                    if (null != express.getExpressNo() && express.getExpressNo().contains(search)) {
                        expresses.add(express);
                    } else if (null != express.getDescription() && express.getDescription().contains(search)) {
                        expresses.add(express);
                    }
                }
                expressList = expresses;
            }

            //搜索后查到的数据条数
            if (expressList.size() < 1) {
                result.put("recordsFiltered", 0);
                result.put("data", Collections.emptyList());
                return result;
            }
            result.put("recordsFiltered", expressList.size());


            if (start > expressList.size()) {
                result.put("data", Collections.emptyList());
            } else {
                int endIdx;
                if (start + length > expressList.size()) {
                    endIdx = expressList.size();
                } else {
                    endIdx = start + length;
                }
                expressList = expressList.subList(start, endIdx);
                result.put("data", expressList);
            }
            return result;
        } else {
            return DBUtils.getEmpty();
        }
    }


    @RequestMapping("/getAlertsByExpressId")
    @ResponseBody
    public Object getAlertsByExpressId(int expressId) {
        if (!validateUser()) {
            return "";
        }
        return alertService.webGetAlertsByExpressId(expressId);
    }

//    @RequestMapping("/deleteAlert/{flag}")
//    public String deleteAlert(Model model, String id, int mode, @PathVariable String flag, RedirectAttributes attr) {
//        Utils.saveLog(lg.getUserId(), "关闭报警", lg.getDomainId());
//        if (!validateUser()) {
//            return "view.login";
//        }
//        DbSession db = BaseHibernateUtils.newSession();
//        try {
//            db.beginTransaction();
//            Date now = new Date();
//            if (!StringUtils.isEmpty(id)) {
//                NDAAlert alert = (NDAAlert) db.get(NDAAlert.class, Integer.parseInt(id));
//                if (alert != null) {
//                    if (mode == 1) {
//                        alert.setLastModitied(now);
//                        attr.addFlashAttribute("message", "报警关闭成功");
//                        alert.setStatus(Constants.AlertState.STATE_FINISHED);
//                    } else {
//                        alert.setLastModitied(now);
//                        attr.addFlashAttribute("message", "报警关闭成功");
//                        alert.setStatus(Constants.State.STATE_FINISHED);
//                    }
//                }
//            }
//            db.commit();
//        } finally {
//            db.close();
//        }
//        attr.addFlashAttribute("error", true);
//        return "redirect:/historyAlerts/" + flag;
//    }

    @RequestMapping("/deleteAlertsByExpressId/{flag}")
    public String deleteAlertsByExpressId(String id, RedirectAttributes attr, @PathVariable
            String flag) {
        Utils.saveLog(lg.getUserId(), "关闭订单号为" + id + "的所有报警");
        if (!validateUser()) {
            return "view.login";
        }
        Response response = alertService.handleAlertsByExpressId(Integer.parseInt(id));
        attr.addFlashAttribute("message", response.getMessage());
        attr.addFlashAttribute("error", true);
        return "redirect:/nowAlerts/" + flag;
    }


    /// 从Levelcontroller转过来
    @RequestMapping("/deleteAlertLevel")
    public String deleteAlertLevel(Model model, String id, int mode, RedirectAttributes
            attr) {
        if (!validateUser()) {
            return "view.login";
        }
        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Date now = new Date();
            if (!StringUtils.isEmpty(id)) {
                AlertLevel user = (AlertLevel) db.get(AlertLevel.class, Integer.parseInt(id));
                if (user != null) {
                    if (mode == 1) {
                        user.setLastModified(now);
                        attr.addFlashAttribute("message", "报警级别关闭成功");
                        user.setStatus(Constants.IsAble.DISABLE);
                        Utils.saveLog(lg.getUserId(), "关闭报警级别");

                    } else {
                        user.setLastModified(now);
                        attr.addFlashAttribute("message", "报警级别启用成功");
                        user.setStatus(Constants.IsAble.ABLE);
                        Utils.saveLog(lg.getUserId(), "启用报警级别");

                    }
                }
            }
            db.commit();
        } finally {
            db.close();
        }

        attr.addFlashAttribute("error", true);

        return "redirect:/level";
    }

    @RequestMapping("/level")
    public String level(Model model) {
        Utils.saveLog(lg.getUserId(), "查看报警级别列表");
        if (!validateUser()) {
            return "redirect:/";
        }

        String view = "alert.level.level";

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return view;
    }

    @RequestMapping("/editLevel")
    public ModelAndView editLevel(Model model, String id) {

        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }

        String view = "alert.level.editLevel";

        AlertLevel level = null;

        if (!StringUtils.isEmpty(id)) {
            model.addAttribute("id", id);
            DbSession session = BaseHibernateUtils.newSession();
            try {
                level = (AlertLevel) session.get(AlertLevel.class, Integer.parseInt(id));
            } finally {
                session.close();
            }
        } else {
            level = new AlertLevel();
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return new ModelAndView(view, "command", level);
    }

    @RequestMapping("/saveLevel")
    public String saveLevel(Model model, String id, String hours, int times, RedirectAttributes attr) {

        if (!validateUser()) {
            return "view.login";
        }

        float hour = 0;

        try {
            hour = Float.parseFloat(hours);
        } catch (Exception e) {
        }

        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Date now = new Date();
            if (!StringUtils.isEmpty(id)) {
                AlertLevel level = (AlertLevel) db.get(AlertLevel.class, Integer.parseInt(id));
                if (level != null) {
                    level.setHours(hour);
                    level.setTimes(times);
                    level.setLastModified(now);
                }
            }

            db.commit();

        } finally {
            db.close();
        }

        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", "报警级别设置成功");
        Utils.saveLog(lg.getUserId(), "设置报警级别");
        return "redirect:/level";
    }

    @RequestMapping("/ajaxLevel")
    @ResponseBody
    public Object ajaxLevel(int draw, int start, int length) {
        if (!validateUser()) {
            return "";
        }
        DbSession session = BaseHibernateUtils.newSession();
        try {
            Map<String, Object> result = query(session, draw, start, length, " order by id ASC ");
            return result;
        } finally {
            session.close();
        }
    }

    private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy) {
        Map<String, Object> result = new HashMap<>();
        String whereClause = "a.domain_id=" + lg.getDomainId();
        long recordsFiltered;
        long recordsTotal = countLevel(db, whereClause);
        result.put("recordsTotal", recordsTotal);

        recordsFiltered = recordsTotal;
        result.put("recordsFiltered", recordsFiltered);
        result.put("data", ajaxLevel(db, whereClause, defaultOrderBy, start, length));
        return result;
    }

    private int countLevel(DbSession session, String where) {

        String sql = "SELECT COUNT(a.*) FROM nda_alert_level a ";

        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        SQLQuery query = session.createSQLQuery(sql);
        BigInteger count = (BigInteger) query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private List<AlertLevel> ajaxLevel(DbSession session, String where, String order, int offset, int limit) {

        String sql = "SELECT * FROM nda_alert_level a ";
        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        if (!StringUtils.isEmpty(order)) {
            sql += (" " + order);
        }

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(AlertLevel.class);
        BaseHibernateUtils.setLimit(query, offset, limit);
        List<?> list = query.list();
        if (!list.isEmpty()) {
            List<AlertLevel> result = new ArrayList<AlertLevel>(list.size());
            for (Object obj : list) {
                AlertLevel e = (AlertLevel) obj;
                e.setStatusKey(e.getStatus() == Constants.IsAble.ABLE ? "启用" : "关闭");
                result.add(e);
            }
            return result;
        }
        return Collections.emptyList();
    }

}
