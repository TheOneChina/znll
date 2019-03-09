package com.tnsoft.web.controller;

import com.expertise.common.logging.Logger;
import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.model.*;
import com.tnsoft.web.service.*;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.AuthUtils;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

@Controller
public class ProtocolController {

    public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();

    // 订单温度图表数据获取
    private final int dataNums = 10000;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource(name = "userService")
    private UserService userService;
    @Resource(name = "expressService")
    private ExpressService expressService;
    @Resource(name = "tempExpressService")
    private TempExpressService tempExpressService;
    @Resource(name = "userExpressService")
    private UserExpressService userExpressService;
    @Resource(name = "alertService")
    private AlertService alertService;
    @Resource()
    private MapService mapService;

    public ProtocolController() {
        super();
    }


    //关闭监测点所有报警
    @RequestMapping(value = "/protocol/handleAlertsByExpressId", method = RequestMethod.POST)
    @ResponseBody
    public void handleAlertsByExpressId(int userId, int expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = alertService.handleAlertsByExpressId(expressId);
        out.write(GSON.toJson(response));
    }


    //删除未成功监测点
    @RequestMapping(value = "/protocol/deleteFailedMonitor", method = RequestMethod.POST)
    @ResponseBody
    public void deleteFailedMonitor(int userId, int expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = expressService.deleteFailedMonitor(expressId);
        out.write(GSON.toJson(response));
    }

    //医药版、标准版获取当前/历史报警监测点列表
    @RequestMapping(value = "/protocol/getAlertMonitors", method = RequestMethod.POST)
    @ResponseBody
    public void getAlertMonitors(int userId, boolean isCurrent, Integer offset, Integer limit, HttpServletResponse
            resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = alertService.getAlertMonitors(userId, isCurrent, null, null, offset, limit);
        out.write(GSON.toJson(response));
    }

    //获取订单的所有报警
    @RequestMapping(value = "/protocol/getAllAlertsByExpressId", method = RequestMethod.POST)
    @ResponseBody
    public void getAllAlertsByExpressId(int userId, int expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = alertService.getAllAlertsByExpressId(expressId);
        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/enableTag", method = RequestMethod.POST)
    @ResponseBody
    public void enableTag(int userId, String tagNo, Integer flag, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = tagService.enableTag(tagNo, userId, flag);
        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/changePwd", method = RequestMethod.POST)
    @ResponseBody
    public void changePwd(int userId, String pwd, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = userService.savePwdWithoutOldPwd(userId, pwd);
        Utils.saveLog(userId, "APP用户修改密码");
        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/login", method = RequestMethod.POST)
    @ResponseBody
    public void login(String name, String pwd, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();
            Criteria criteria = db.createCriteria(User.class);
            criteria.add(Restrictions.eq("name", name));
            User account = (User) criteria.uniqueResult();
            if (account == null) {
                Response response = new Response(Response.ERROR);
                out.write(GSON.toJson(response));
                return;
            }

            int auth = AuthUtils.authWithPassword(account, pwd, false);
            switch (auth) {
                case AuthUtils.AUTH_OK:
                    break;
                case AuthUtils.AUTH_FAILED:
                case AuthUtils.AUTH_ATTEMPT_EXCEED:
                case AuthUtils.AUTH_DISABLED:
                    db.commit();
                    Response response = new Response(auth);
                    out.write(GSON.toJson(response));
                    return;
                default:
                    throw new IllegalStateException("Invalid auth result: " + auth);
            }

            Response response = new Response(Response.OK);

            response.setRoleId(userService.getUserRole(account.getId()).get(0).getId());
            //因APP无人开发，所以将新的角色更换为APP支持的老角色，不影响使用
            if (response.getRoleId() == Constants.Role.SUB_ADMIN_MEDICINE) {
                response.setRoleId(Constants.Role.ADMIN_MEDICINE);
            }
            db.commit();

            String ticket = account.getTicket();
            response.setTicket(ticket);
            response.setUserId(account.getId());
            response.setMessage(account.getNickName());

            Utils.saveLog(account.getId(), "APP用户登录");

            out.write(GSON.toJson(response));
            return;
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    //创建监测点
    @RequestMapping(value = "/protocol/createMonitor", method = RequestMethod.POST)
    @ResponseBody
    public void createMonitor(int userId, String monitorName, String tagNo, String description, Integer flag,
                              HttpServletResponse
                                      resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        if (null == flag) {
            flag = 2;
        }
        Response response = expressService.saveTakingExpress(userId, monitorName, tagNo, description, null, null, flag);
        out.write(GSON.toJson(response));
    }

    // 揽收
    @RequestMapping(value = "/protocol/gather_v1", method = RequestMethod.POST)
    @ResponseBody
    public void gather(String data, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = new Response(Response.OK);
        ExpressGatherInfo expressGatherInfo = GSON.fromJson(data, ExpressGatherInfo.class);
        List<ExpressGatherInfo.ExpressNoAndDescription> list = expressGatherInfo.getExpressNoAndDescriptions();
        response.setTicket("");
        if (null != list && !list.isEmpty()) {
            for (ExpressGatherInfo.ExpressNoAndDescription expressNoAndDescription : list) {
                String expressNo = expressNoAndDescription.getExpressNo();
                if (StringUtils.isEmpty(expressNo)) {
                    continue;
                }
                Response responseTemp = expressService.saveTakingExpress(expressGatherInfo.getUserId(), expressNo,
                        expressGatherInfo.getTagNo(), expressNoAndDescription.getDescription(), null, null, 1);
                if (responseTemp.getCode() == Response.ERROR) {
                    response.setCode(Response.ERROR);
                    response.setMessage(responseTemp.getMessage());
                    response.setTicket(response.getTicket() + expressNo + ";");
                }
            }
        }
        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/exchange", method = RequestMethod.POST)
    @ResponseBody
    public void exchange(int userId, String expressNo, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = new Response(Response.OK);
        Utils.saveLog(userId, "APP转运货物");
        StringBuilder sb = new StringBuilder();

        // 获得当前用户的domainId
        int domainId = userService.getDomainIdByUserId(userId);
        String[] allExpressNoStr = expressNo.split(",");

        // 遍历所有的订单编号,更改状态
        for (String expressNoStr : allExpressNoStr) {
            Express express = expressService.getExpressByExpressNoAndDomainId(expressNoStr, domainId);
            if (null == express) {
                sb.append(expressNoStr).append(",该订单未揽收;");
            } else if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
                sb.append(expressNoStr).append(",订单已经签收;");
            } else {
                //先将该订单与他人的关系解除，为处理多个关系同时活动存在的脏数据，使用list将其全部置为关闭；
                userExpressService.setAllActiveUserExpressToFinishedByEId(express.getId());

                boolean result = userExpressService.createUserExpressRelation(userId, express.getId());
                if (result) {
                    sb.append(expressNoStr).append(",订单承运成功;");
                } else {
                    sb.append(expressNoStr).append(",订单承运失败;");
                }
            }
        }
        response.setMessage(sb.toString());
        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/signing", method = RequestMethod.POST)
    @ResponseBody
    public void signing(int userId, String expressNo, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();

        int domainId = userService.getDomainIdByUserId(userId);
        Utils.saveLog(userId, "APP签收货物");
        String[] expressNoArray = expressNo.split(",");
        Set<Integer> set = new HashSet<>();
        List<Integer> ids = new ArrayList<>();
        for (String anExpressNoArray : expressNoArray) {
            if (anExpressNoArray.isEmpty()) {
                continue;
            }
            Express express = expressService.getExpressByExpressNoAndDomainId(anExpressNoArray, domainId);
            if (null == express) {
                continue;
            }
            Integer id = express.getId();
            set.add(id);
            ids.add(id);
        }
        Response response = new Response(Response.ERROR);
        if (set.size() < 1) {
            response.setMessage("请输入正确的值");
        } else {
            response = expressService.signExpressByIdList(set);

            List<Express> list = expressService.getExpressListByIds(ids);
            if (null != list) {
                response.setExpress(list);
            }
        }

        out.write(GSON.toJson(response));
    }

    @RequestMapping(value = "/protocol/uploadLocation", method = RequestMethod.POST)
    @ResponseBody
    public void uploadLocation(String data, HttpServletResponse resp)
            throws IOException {

        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = new Response(Response.ERROR);
        LocationWithExpressNoList locationWithExpressNoList = GSON.fromJson(data, LocationWithExpressNoList.class);


        if (null == locationWithExpressNoList || null == locationWithExpressNoList.getExpressNoList()) {
            out.write(GSON.toJson(response));
            return;
        }

        int userId = locationWithExpressNoList.getUserId();
        List<String> expressNoList = locationWithExpressNoList.getExpressNoList();

        if (expressNoList.size() > 0) {

            List<Integer> expressIdList = expressService.getExpressListByExpressNoList(userId, expressNoList);
            if (null != expressIdList && expressIdList.size() > 0) {
                mapService.saveLocation(userId, locationWithExpressNoList.getLat(), locationWithExpressNoList.getLng
                        (), expressIdList);
                response.setCode(Response.OK);
                out.write(GSON.toJson(response));
                return;
            }
        }
        out.write(GSON.toJson(response));
    }

    // 当前订单
    @RequestMapping(value = "/protocol/express", method = RequestMethod.POST)
    @ResponseBody
    public void express(int userId, Integer offset, Integer limit, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();

        Utils.saveLog(userId, "APP查看当前订单列表");
        Response r = new Response(Response.OK);
        List<Express> list = expressService.getExpressList(userId, true, true, offset, limit, null, null, null, false);
        if (null != list) {
            r.setExpress(list);
            for (Express express : list) {
                express.setDataComplete(expressService.isDataComplete(express));
            }
        }
        out.write(GSON.toJson(r));

    }

    // 误操作时恢复为待配送
    @RequestMapping(value = "/protocol/signToTaking", method = RequestMethod.POST)
    @ResponseBody
    public void signToTaking(int userId, int expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = expressService.cancelSign(userId, expressId);
        out.write(GSON.toJson(response));
    }

    // 订单温度
    @RequestMapping(value = "/protocol/expressTmp", method = RequestMethod.POST)
    @ResponseBody
    public void expressTmp(int userId, int expressId, int offset, int limit, HttpServletResponse resp)
            throws IOException {
        Logger.error("查看订单温度，订单序号: " + expressId);
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {
            Response r = new Response(Response.OK);
            if (userId > 0) {
                Utils.saveLog(userId, "APP查看订单温度");
            }
            Criteria criteria = db.createCriteria(TempExpress.class);
            criteria.add(Restrictions.eq("expressId", expressId));
            criteria.addOrder(Order.desc("creationTime"));

            if (offset >= 0 && limit > 0) {
                BaseHibernateUtils.setLimit(criteria, offset * limit, limit);
            }

            List<TempExpress> list = criteria.list();
            r.setTemps(list);

            out.write(GSON.toJson(r));
            return;

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    @RequestMapping(value = {"/protocol/getTags", "/protocol/getTagsBySearch"}, method = RequestMethod.POST)
    @ResponseBody
    public void getTags(int userId, int offset, int limit, String search, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {
            //Response r = new Response(Response.OK);
            Result r = new Result();
            r.setCode(Result.OK);
            if (userId > 0) {
                Utils.saveLog(userId, "APP查看设备列表");
            }
            int domainId = DBUtils.getRootDomainIdByUserId(db, userId);
            String hql;
            if (StringUtils.isEmpty(search)) {
                hql = "select new Tag(tagNo,name) from Tag where domainId=? order by creationTime desc ";
            } else {
                hql = "select new Tag(tagNo,name) from Tag where domainId=? and (tagNo LIKE '%" + search + "%' OR " +
                        "name LIKE '%" + search + "%') order by creationTime desc";
            }

            Query query = db.createQuery(hql).setParameter(0, domainId);
            //默认查询出来的list里存放的是一个Object对象，但是在这里list里存放的不再是默认的Object对象了，而是Link对象了
            if (offset >= 0 && limit > 0) {
                BaseHibernateUtils.setLimit(query, offset * limit, limit);
            }
            List<Tag> links = query.list();
            r.setTags(links);
            out.write(GSON.toJson(r));
            return;

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Result(Result.ERROR)));
    }

    @RequestMapping(value = "/protocol/getTagInfo", method = RequestMethod.POST)
    @ResponseBody
    public void getTagInfoByNoAndUserId(String tagNo, int userId, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();

        Response r = new Response(Response.ERROR);
        Tag tag = tagService.getTagInfoByNoAndUserId(tagNo, userId);
        if (null != tag) {
            r.setCode(Response.OK);
            r.setTag(tag);
        }
        out.write(GSON.toJson(r));
    }

    // 删除设备,暂时医药版调用
    @RequestMapping(value = "/protocol/deleteTag", method = RequestMethod.POST)
    @ResponseBody
    public void deleteTag(String tagNo, int userId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = tagService.deleteTag(tagNo, userId);
        out.print(GSON.toJson(response));
    }

    // 设备上传周期
    @RequestMapping(value = "/protocol/ajaxEditSleepTime", method = RequestMethod.POST)
    @ResponseBody
    public void setSleepTime(String tagNo, String time, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = tagService.editSleepTime(tagNo, Integer.parseInt(time));
        out.print(GSON.toJson(response));
    }

    //设置设备受警人
    @RequestMapping(value = "/protocol/setTagAlertPhones", method = RequestMethod.POST)
    @ResponseBody
    public void setTagAlertPhones(String tagNo, String alertPhones, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = tagService.editAlertPhones(tagNo, alertPhones);
        out.print(GSON.toJson(response));
    }

    //查询监测点是否成功
    @RequestMapping(value = "/protocol/getMonitorsStatus", method = RequestMethod.POST)
    @ResponseBody
    public void getMonitorsStatus(int userId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = expressService.getMonitorsStatus(userId);
        out.print(GSON.toJson(response));
    }

//    @RequestMapping(value = "/protocol/setTagStatus", method = RequestMethod.POST)
//    @ResponseBody
//    public void setTagStatus(String id, String mode, HttpServletResponse resp) throws IOException {
//        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
//        PrintWriter out = resp.getWriter();
//        DbSession db = BaseHibernateUtils.newSession();
//        Result r = new Result();
//        try {
//            db.beginTransaction();
//            Date now = new Date();
//            if (!StringUtils.isEmpty(id)) {
//                Tag tag = (Tag) db.get(Tag.class, id);
//                if (tag != null) {
//                    tag.setLastModitied(now);
//                    if (Integer.parseInt(mode) == 1) {
//                        tag.setStatus(Constants.TagState.STATE_DELETE);
//                    } else {
//                        tag.setStatus(Constants.TagState.STATE_ACTIVE);
//                        if (DBUtils.hasBind(db, tag.getTagNo())) {
//                            tag.setStatus(Constants.TagState.STATE_WORKING);
//                        }
//                    }
//                }
//            }
//            r.setCode(Result.OK);
//            db.commit();
//        } finally {
//            db.close();
//        }
//        out.write(GSON.toJson(r));
//    }

    // 设备AP设置
//    @RequestMapping(value = "/protocol/setAPStatus", method = RequestMethod.POST)
//    @ResponseBody
//    public void setAPconfig(String name, String password, String tagNo, HttpServletResponse resp) throws IOException {
//        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
//        PrintWriter out = resp.getWriter();
//        Result r = new Result();
//        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(password)) {
//            r.setCode(Result.ERROR);
//            r.setMessage("不能为空！");
//        } else if (!Utils.isValidAPName(name)) {
//            r.setCode(Result.ERROR);
//            r.setMessage("AP名应为字母数字组合，不低于4位！");
//        } else if (!Utils.isValidAPPasswd(password)) {
//            r.setCode(Result.ERROR);
//            r.setMessage("AP密码应为字母数字组合，不低于8位！");
//        } else {
//            Response res = tagService.saveTagAPConfig(name, password, tagNo);
//            r.setCode(res.getCode() + "");
//            r.setMessage(res.getMessage());
//        }
//        out.print(GSON.toJson(r));
//    }

    @RequestMapping("/protocol/editBuzzer")
    @ResponseBody
    public void editBuzzer(String tagNo, int mode, RedirectAttributes attr, HttpServletResponse resp) throws
            IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Result r = new Result();
        // 该参数model不是modelAndView中的Model,是蜂鸣器的关闭模式还是开启模式
        Response res = tagService.editBuzzer(tagNo, mode);
        r.setCode(res.getCode() + "");
        r.setMessage(res.getMessage());
        out.print(GSON.toJson(r));
    }

    @RequestMapping("/protocol/editAppointStart")
    @ResponseBody
    public void editAppointStart(String id, String time, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Result r = new Result();
        Response res = tagService.editAppointStart(id, time);
        r.setCode(res.getCode() + "");
        r.setMessage(res.getMessage());
        out.print(GSON.toJson(r));
    }

    @RequestMapping(value = "/protocol/getTagInfoByEid", method = RequestMethod.POST)
    @ResponseBody
    public void getTagByExpressId(int expressId, HttpServletResponse resp)
            throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response r = new Response(Response.ERROR);
        if (expressId < 1) {
            out.write(GSON.toJson(r));
            return;
        }
        Tag tag = tagService.getTagByEId(expressId);
        if (null == tag) {
            out.write(GSON.toJson(r));
            return;
        }
        r.setTag(tag);
        r.setCode(Response.OK);
        out.write(GSON.toJson(r));
    }

    @RequestMapping(value = "/protocol/expressChart", method = RequestMethod.POST)
    @ResponseBody
    public void expressChart(int userId, int expressId, HttpServletResponse resp) throws IOException {
        Logger.error("获取订单温度图表数据，订单序号: " + expressId);
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {
            Response r = new Response(Response.OK);
            if (userId > 0) {
                Utils.saveLog(userId, "APP获取订单温度图表数据");
            }

            List<TempExpress> list = DBUtils.getAllTempesByExpressId(db, expressId);

            if (list.size() <= dataNums) {
                r.setTemps(list);
            } else {
                r.setTemps(list.subList(0, dataNums));
            }
            out.write(GSON.toJson(r));
            return;
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    @RequestMapping(value = "/protocol/getTempExpressByDate", method = RequestMethod.POST)
    @ResponseBody
    public void getTempExpressByDate(int userId, int expressId, String start, String end, HttpServletResponse resp)
            throws IOException {

        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Date startDate = null;
        Date endDate = null;
        if (null != start) {
            try {
                startDate = Utils.SF.parse(start);
            } catch (ParseException e) {
                startDate = null;
                e.printStackTrace();
            }
        }
        if (null != end) {
            try {
                endDate = Utils.SF.parse(end);
            } catch (ParseException e) {
                endDate = null;
                e.printStackTrace();
            }
        }

        Response response = tempExpressService.getByExpressIdWithDateLimit(expressId, startDate, endDate);

        out.write(GSON.toJson(response));
    }

    // 2017-4-11 app获取订单报表信息
    @RequestMapping(value = "/protocol/expressBriefInfo", method = RequestMethod.POST)
    @ResponseBody
    public void expressBriefInfo(int userId, int expressId, HttpServletResponse resp) throws IOException {
        Logger.error(userId + "获取订单报表数据，订单序号: " + expressId);
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Map<String, String> result = tempExpressService.getBriefInfoByExpressId(expressId);

        out.write(GSON.toJson(result));
    }


    @RequestMapping("/protocol/saveTemperature")
    @ResponseBody
    public void saveTemperature(Model model, String tagNo, Float temperatureMin, Float temperatureMax,
                                HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = tagService.editTemperature(tagNo, temperatureMin, temperatureMax);
        out.write(GSON.toJson(response));
    }

    // 历史订单
    @RequestMapping(value = "/protocol/expressHis", method = RequestMethod.POST)
    @ResponseBody
    public void expressHis(int userId, int offset, int limit, HttpServletResponse resp) throws IOException {

        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();

        Utils.saveLog(userId, "APP查看历史订单");
        Response r = new Response(Response.OK);
        List<Express> list = expressService.getExpressList(userId, false, true, offset, limit, null, null, null, false);
        if (null != list) {
            r.setExpress(list);
        }
        out.write(GSON.toJson(r));
    }

    // 报警信息
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/protocol/alerts", method = RequestMethod.POST)
    @ResponseBody
    public void alerts(int userId, long csn, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {

            Utils.saveLog(userId, "APP查看报警列表");

            Response r = new Response(Response.OK);
            List<Role> roles = userService.getUserRole(userId);
            String sql;
            if (null != roles && !roles.isEmpty() && roles.get(0).isDomainRights()) {
                int domainId = DBUtils.getDomainIdByUserId(db, userId);
                sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
                        + "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE status =" + Constants
                        .ExpressState.STATE_ACTIVE + " and domain_id=" + domainId
                        + ") order by a.creation_time DESC ";
            } else {
                sql = "SELECT * FROM nda_alert a WHERE a.status=" + Constants.AlertState.STATE_ACTIVE
                        + "AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE status =" + Constants
                        .ExpressState.STATE_ACTIVE + " and user_id=" + userId
                        + ") order by a.creation_time DESC ";
            }

            SQLQuery query = db.createSQLQuery(sql);
            query.addEntity(NDAAlert.class);
            BaseHibernateUtils.setLimit(query, 0, 10);
            List<?> list = query.list();
            if (!list.isEmpty()) {
                List<Alert> alerts = new ArrayList<>();
                for (Object obj : list) {
                    NDAAlert a = (NDAAlert) obj;
                    Alert alert = new Alert();
                    alert.setAlertLevel(a.getAlertLevel());
                    alert.setCreationTime(a.getCreationTime());
                    alert.setDomainId(a.getDomainId());
                    alert.setId(a.getId());
                    alert.setLastModitied(a.getLastModitied());
                    alert.setStatus(a.getStatus());
                    alert.setTagNo(a.getTagNo());
                    alert.setType(a.getType());
                    List<Express> exs = new ArrayList<>();
                    exs.add(getExpressById(db, a.getExpressId()));
                    alert.setExpress(exs);

                    r.setExtra(Math.max(r.getExtra(), alert.getCsn()));

                    alerts.add(alert);
                }

                Collections.sort(alerts, new Comparator() {
                    @Override
                    public int compare(Object a, Object b) {
                        int one = ((Alert) a).getStatus();
                        int two = ((Alert) b).getStatus();
                        return one - two;
                    }
                });

                r.setAlerts(alerts);
            }

            String result = GSON.toJson(r);
            out.write(result);
            return;

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    // 分页获取所有报警订单
    @RequestMapping(value = "/protocol/hisAlertExpresses", method = RequestMethod.POST)
    @ResponseBody
    public void hisAlertExpresses(int userId, int offset, int limit, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {

            Utils.saveLog(userId, "APP查看历史报警订单列表");

            Response r = new Response(Response.OK);

            int roleId = DBUtils.getRoleByUserId(db, userId).getRoleId();
            String sql;
            if (roleId == Constants.Role.ADMIN) {
                int domainId = DBUtils.getDomainIdByUserId(db, userId);
                sql = "SELECT * FROM nda_express WHERE id IN (SELECT a.express_id FROM nda_alert a WHERE a.status="
                        + Constants.AlertState.STATE_FINISHED + " AND a.domain_id=" + domainId
                        + " order by a.creation_time DESC )";
            } else {
                sql = "SELECT * FROM nda_express WHERE id IN (SELECT a.express_id FROM nda_alert a WHERE a.status="
                        + Constants.AlertState.STATE_FINISHED
                        + " AND a.express_id IN (SELECT express_id FROM nda_user_express WHERE user_id=" + userId
                        + ") order by a.creation_time DESC) ";
            }

            SQLQuery query = db.createSQLQuery(sql);
            query.addEntity(Express.class);
            if (offset >= 0 && limit > 0) {
                BaseHibernateUtils.setLimit(query, offset * limit, limit);
            }
            List<Express> list = query.list();
            if (!list.isEmpty()) {
                r.setExpress(list);
            }
            String result = GSON.toJson(r);
            out.write(result);
            return;
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }


    // 未使用
    // 根据订单id获取订单历史报警
    @RequestMapping(value = "/protocol/getExpressAlerts", method = RequestMethod.POST)
    @ResponseBody
    public void getExpressAlerts(int userId, int expressId, int offset, int limit, HttpServletResponse resp)
            throws IOException {

        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {

            Utils.saveLog(userId, "APP查看订单报警详情");
            Response r = new Response(Response.OK);
            String sql = "SELECT * FROM nda_alert WHERE express_id=" + expressId + " AND status="
                    + Constants.AlertState.STATE_FINISHED + " order by creation_time DESC ";
            SQLQuery query = db.createSQLQuery(sql);
            query.addEntity(NDAAlert.class);
            if (offset >= 0 && limit > 0) {
                BaseHibernateUtils.setLimit(query, offset * limit, limit);
            }
            List<NDAAlert> list = query.list();
            if (!list.isEmpty()) {
                r.setNdaAlerts(list);
            }
            String result = GSON.toJson(r);
            out.write(result);
            return;
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    // 关闭一般报警
    @RequestMapping(value = "/protocol/closeAlert", method = RequestMethod.POST)
    @ResponseBody
    public void closeAlert(int userId, int alertId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Utils.saveLog(userId, "APP关闭报警");

            Response r = new Response(Response.OK);

            String sql = "SELECT * FROM nda_alert WHERE express_id IN ("
                    + " SELECT express_id FROM nda_user_express WHERE user_id=" + userId + ")";

            SQLQuery query = db.createSQLQuery(sql);
            query.addEntity(NDAAlert.class);
            List<?> list = query.list();
            if (!list.isEmpty()) {
                for (Object object : list) {
                    NDAAlert ndaAlert = (NDAAlert) object;
                    if (ndaAlert != null) {
                        if (ndaAlert.getId() <= alertId) {
                            ndaAlert.setStatus(Constants.AlertState.STATE_FINISHED);
                        }
                    }
                }
            }

            db.commit();

            out.write(GSON.toJson(r));
            return;

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            db.close();
        }
        out.write(GSON.toJson(new Response(Response.ERROR)));
    }

    private Express getExpressById(DbSession db, int expressId) {
        String sql = "SELECT * FROM nda_express WHERE id=" + expressId;
        SQLQuery query = db.createSQLQuery(sql);
        query.addEntity(Express.class);
        return (Express) query.list().get(0);
    }

    @RequestMapping(value = "/protocol/electricityByTag ", method = RequestMethod.POST)
    @ResponseBody
    public Object getElectricityByTag(Integer userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Tag> tags = tagService.getTagByUId(userId);
        for (Tag tag : tags) {
            if (null != tag.getElectricity() && tag.getElectricity() != 1 && tag.getElectricity() < Constants.Electricity.NORMAL) {
                // 将需要传到app的电量和设备编号,封装为map,放入list
                Map<String, Object> map = new HashMap<>();
                map.put("tagNo", tag.getTagNo());
                map.put("electricity", tag.getElectricity());
                list.add(map);
            }
        }
        return list;
    }

    @RequestMapping(value = "/protocol/expressAttribute", method = RequestMethod.POST)
    @ResponseBody
    public Object saveExpressAttribute(String expressValue, String expressFlag, int userId, int expressId) {
        return expressService.saveExpressAttribute(expressValue, expressFlag, userId, expressId);
    }

    @RequestMapping(value = "/protocol/queryByExpressNo", method = RequestMethod.POST)
    @ResponseBody
    public void queryByExpressNo(int userId, String expressNo, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        Response response = new Response(Response.OK);
        List<Express> expresses = expressService.queryByExpressNo(userId, expressNo);
        if (null != expresses && expresses.size() > 0) {
            response.setExpress(expresses);
        }
        out.write(GSON.toJson(response));
    }
}

