package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.TempExpress;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.service.TempExpressService;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.Utils;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class TempHistoryController extends BaseController {

    public static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();

    @Resource(name = "tempExpressService")
    private TempExpressService tempExpressService;

    public TempHistoryController() {
        super();
    }


    @RequestMapping("/temperatureLog")
    public String temperatureLog(Model model, String id) {
        Utils.saveLog(lg.getUserId(), "查看温度历史记录");
        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("expressNo", id);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return "view.tag.temperatureLog";
    }

    @RequestMapping("/temperatureHistory")
    public String temperatureHistory(Model model, String id) {

        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("expressId", id);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return "view.tag.temperatureHistory";
    }


    //曲线图
    @RequestMapping("/ajaxTemperature")
    @ResponseBody
    public void ajaxTemperature(String expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Result result;
        if (!StringUtils.isEmpty(expressId)) {
            result = tempExpressService.queryByExpressId(Integer.parseInt(expressId));
        } else {
            result = new Result(Result.ERROR);
        }
        out.write(GSON.toJson(result));
    }


    @RequestMapping("/ajaxTempHistory")
    @ResponseBody
    public Object ajaxTempHistory(int draw, int start, int length, String expressId) {
        if (!validateUser()) {
            return "";
        }

        DbSession session = BaseHibernateUtils.newSession();
        try {
            if (!StringUtils.isEmpty(expressId)) {
                Map<String, Object> result = query(session, draw, start, length, Integer.parseInt(expressId));
                return result;
            }
        } finally {
            session.close();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("recordsTotal", 0);
        result.put("recordsFiltered", 0);
        result.put("data", Collections.<TempExpress>emptyList());
        return result;
    }

    private Map<String, Object> query(DbSession db, int draw, int start, int length, int expressId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String[]> properties = request.getParameterMap();

        String orderSql = " ORDER BY creation_time DESC";

        String search = properties.get("search[value]")[0];
        String whereClause = "a.express_id=" + expressId;
        long recordsFiltered = 0;
        long recordsTotal = count(db, whereClause);
        result.put("recordsTotal", recordsTotal);

        if (!StringUtils.isEmpty(search)) {
            recordsFiltered = count(db, whereClause);
        } else {
            recordsFiltered = recordsTotal;
        }
        result.put("recordsFiltered", recordsFiltered);

        result.put("data", query(db, whereClause, orderSql, start, length));
        return result;

    }

    private int count(DbSession session, String where) {

        String sql = "SELECT COUNT(a.*) FROM nda_temperature_express a ";

        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        SQLQuery query = session.createSQLQuery(sql);
        BigInteger count = (BigInteger) query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private List<TempExpress> query(DbSession session, String where, String order, int offset, int limit) {

        String sql = "SELECT * FROM nda_temperature_express a ";
        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        if (!StringUtils.isEmpty(order)) {
            sql += (" " + order);
        }

        SQLQuery query = session.createSQLQuery(sql);
        query.addEntity(TempExpress.class);
        BaseHibernateUtils.setLimit(query, offset, limit);
        List<?> list = query.list();
        if (!list.isEmpty()) {
            List<TempExpress> result = new ArrayList<TempExpress>(list.size());
            for (Object obj : list) {
                TempExpress e = (TempExpress) obj;
                e.setTmpValue(String.format("%.2f", e.getTemperature()));
                e.setHumidityValue(String.format("%.2f", e.getHumidity()));
                e.setTimeValue(SF.format(e.getCreationTime()));
                result.add(e);
            }
            return result;
        }

        return Collections.emptyList();
    }
}
