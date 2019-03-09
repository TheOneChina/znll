package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.hibernate.model.UserExpress;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.ExportService;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.TempExpressService;
import com.tnsoft.web.service.UserExpressService;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ExpressController extends BaseController {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting()
            .create();
    @Resource(name = "expressService")
    private ExpressService expressService;
    @Resource(name = "tempExpressService")
    private TempExpressService tempExpressService;
    @Resource(name = "userExpressService")
    private UserExpressService userExpressService;
    @Resource(name = "exportService")
    private ExportService exportService;

    public ExpressController() {
        super();
    }

    //揽收订单或创建监测点页面
    @RequestMapping("/takingExpress/{flag}")
    public String takingExpress(Model model, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "揽收货物");
        if (!validateUser()) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.taking";
                break;
            case "2":
                view = "express.medicine.taking";
                break;
            case "3":
                view = "express.standard.taking";
                break;
            default:
        }
        return view;
    }


    //提交揽收订单或创建监测点
    @RequestMapping("/saveTakingExpress/{flag}")
    public String saveTakingExpress(String expressNo, String tagNo, String description, Integer appointStart,
                                    Integer appointEnd, RedirectAttributes attr, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "揽收订单:" + expressNo);
        if (!validateUser()) {
            return "redirect:/";
        }
        Response res = expressService.saveTakingExpress(lg.getUserId(), expressNo, tagNo, description, appointStart,
                appointEnd, Integer.valueOf(flag));

        attr.addFlashAttribute("username", lg.getUserName());
        attr.addFlashAttribute("rolename", lg.getDefRole().getName());
        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", res.getMessage());

        return "redirect:/takingExpress/" + flag;
    }


    @RequestMapping("/expressTemperatureHis/{flag}")
    public String expressTemperatureHis(Model model, String id, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        model.addAttribute("expressId", id);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.temperatureHistory";
                break;
            case "2":
                view = "express.medicine.temperatureHistory";
                break;
            case "3":
                view = "express.standard.temperatureHistory";
                break;
            default:
        }
        return view;
    }


    @RequestMapping(value = "/saveEndMonitor")
    public String saveEndMonitor(String expressId, String userId, RedirectAttributes attr) {
        Response res;
        if (!validateUser()) {
            return "redirect:/";
        }
        Set<Integer> set = new HashSet<>();
        if (!expressId.isEmpty()) {
            set.add(Integer.parseInt(expressId));
        }
        res = expressService.signExpressByIdList(set);
        attr.addFlashAttribute("username", lg.getUserName());
        attr.addFlashAttribute("rolename", lg.getDefRole().getName());
        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", res.getMessage());
        return "redirect:/express/2";
    }

    @RequestMapping(value = "/saveSignExpress") // 执行签收动作,注意接收Integer数组
    @ResponseBody
    public Response saveSignExpress(String[] expressIdList, String userId, RedirectAttributes attr) {
        Utils.saveLog(lg.getUserId(), "签收货物");
        Response res;
        if (!validateUser()) {
            res = new Response(Response.ERROR);
            res.setMessage("签收失败！");
            return res;
        }
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < expressIdList.length; i++) {
            if (expressIdList[i].isEmpty()) {
                continue;
            }
            set.add(Integer.parseInt(expressIdList[i]));
        }

        res = expressService.signExpressByIdList(set);
        attr.addFlashAttribute("username", lg.getUserName());
        attr.addFlashAttribute("rolename", lg.getDefRole().getName());
        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", res.getMessage());
        return res;
    }

    @RequestMapping("/toWindowExpress/{flag}")
    public String toWindowExpress(String[] expressIdList, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        // 传过来的是list
        session.setAttribute("expressIdList", expressIdList);
        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.windowExpress";
                break;
            case "2":
                view = "express.medicine.windowExpress";
                break;
            case "3":
                view = "express.standard.windowExpress";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/windowExpress")
    @ResponseBody
    public Object windowExpress(HttpSession session, RedirectAttributes attr) {
        if (!validateUser()) {
            return "redirect:/";
        }
        // 返回各个视窗的温湿度曲线
        return (String[]) session.getAttribute("expressIdList");
    }

    @RequestMapping("/windowTemHum/{flag}")
    public String temHum(Model model, String expressId, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        model.addAttribute("expressId", expressId);
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "1":
                view = "temHum.express";
                break;
            case "2":
                view = "temHum.medicine";
                break;
            case "3":
                view = "temHum.standard";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/exchangerExpress")
    public String exchangerExpress(Model model) {

        Utils.saveLog(lg.getUserId(), "转运货物");
        if (!validateUser()) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return "express.express.exchanger";
    }

    @RequestMapping("/expressTemperature/{flag}")
    public ModelAndView expressTemperature(Model model, String id, @PathVariable String flag) {

        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }

        Express express = null;

        if (!StringUtils.isEmpty(id)) {
            model.addAttribute("id", id);
            express = expressService.getById(Integer.parseInt(id));
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.temperature";
                break;
            case "2":
                view = "express.medicine.temperature";
                break;
            case "3":
                view = "express.standard.temperature";
                break;
            default:
        }
        return new ModelAndView(view, "command", express);
    }

    @RequestMapping("/saveExpressTemperature/{flag}")
    public String saveExpressTemperature(Model model, String id, String temperatureMin, String temperatureMax,
                                         RedirectAttributes attr, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        Utils.saveLog(lg.getUserId(), "设置订单温度阀值，低温：" + temperatureMin + " 高温:" + temperatureMax);
        if (!StringUtils.isEmpty(id)) {
            Float low = null;
            Float high = null;
            if (!StringUtils.isEmpty(temperatureMin)) {
                low = Float.parseFloat(temperatureMin);
            }
            if (!StringUtils.isEmpty(temperatureMax)) {
                high = Float.parseFloat(temperatureMax);
            }

            Response response = expressService.editTemperature(Integer.parseInt(id), low, high);
            if (response.getCode() == Response.OK) {
                attr.addFlashAttribute("message", "温度阈值设置成功");
            } else {
                attr.addFlashAttribute("message", "温度阈值设置失败");
            }
        } else {
            attr.addFlashAttribute("message", "温度阈值设置失败");
        }
        attr.addFlashAttribute("error", true);

        return "redirect:/express/" + flag;
    }

    @RequestMapping("/express/{flag}")
    public String express(Model model, @PathVariable String flag) {

        if (!validateUser()) {
            return "redirect:/";
        }

        Utils.saveLog(lg.getUserId(), "查看当前订单列表");

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.express";
                break;
            case "2":
                view = "express.medicine.express";
                break;
            case "3":
                view = "express.standard.express";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/history/{flag}")
    public String historyExpress(Model model, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        Utils.saveLog(lg.getUserId(), "查看历史订单列表");

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "1":
                view = "express.express.history";
                break;
            case "2":
                view = "express.medicine.history";
                break;
            case "3":
                view = "express.standard.history";
                break;
            default:
        }
        return view;
    }

    //获取订单报表
    @RequestMapping("/getExpressBriefInfo")
    @ResponseBody
    public void getExpressBriefInfo(int expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        if (!validateUser() || expressId < 0) {
            return;
        }
        Map<String, String> result = tempExpressService.getBriefInfoByExpressId(expressId);

        out.write(GSON.toJson(result));
    }

    // 转运订单处理
    @RequestMapping("/saveExchangeExpress")
    public String saveExchangeExpress(Model model, String expressNo, RedirectAttributes attr) {
        Utils.saveLog(lg.getUserId(), "转运订单:" + expressNo);
        if (!validateUser()) {
            return "redirect:/";
        }
        if (!StringUtils.isEmpty(expressNo)) {
            // 获得当前订单
            Express express = expressService.getExpressByExpressNoAndDomainId(expressNo, lg.getRootDomainId());

            if (null == express) {
                attr.addFlashAttribute("message", "该订单未揽收");
            } else if (express.getStatus() == Constants.ExpressState.STATE_FINISHED) {
                attr.addFlashAttribute("message", "订单已经签收");
            } else {
                //先将该订单与他人的关系解除，为处理多个关系同时活动存在的脏数据，使用list将其全部置为关闭；
                userExpressService.setAllActiveUserExpressToFinishedByEId(express.getId());

                boolean result = userExpressService.createUserExpressRelation(lg.getUserId(), express.getId());
                if (result) {
                    attr.addFlashAttribute("message", "订单承运成功！");
                } else {
                    attr.addFlashAttribute("message", "订单承运失败！");
                }
            }
        }

        attr.addFlashAttribute("username", lg.getUserName());
        attr.addFlashAttribute("rolename", lg.getDefRole().getName());
        attr.addFlashAttribute("error", true);
        return "redirect:/exchangerExpress";
    }


    @RequestMapping("/getExpressListCount")
    @ResponseBody
    public Object getExpressListCount(String startTime, String endTime, String userName, boolean isCurrent) {
        if (!validateUser()) {
            return DBUtils.getEmpty();
        }

        Map<String, String> result = new HashMap<>();

        List<Express> expressList = expressService.getExpressList(lg.getUserId(), isCurrent, true, null, null,
                startTime, endTime, userName, true);

        int alertExpressCount = 0;
        int handledAlertCount = 0;
        int unhandledAlertCount = 0;
        int unhandledAlertExpressCount = 0;
        Set<String> onlineTag = new HashSet<>();
        Set<String> offlineTag = new HashSet<>();
        if (expressList.size() > 0) {
            for (Express express : expressList) {
                int handled = express.getHistoryAlertCount();
                int unhandled = express.getAlertCount();
                if (handled + unhandled > 0) {
                    alertExpressCount++;
                }
                handledAlertCount += handled;
                unhandledAlertCount += unhandled;
                if (unhandled > 0) {
                    unhandledAlertExpressCount++;
                }

                String tagNo = expressService.getTagNoByExpressId(express.getId());
                if (null == tagNo || onlineTag.contains(tagNo) || offlineTag.contains(tagNo)) {
                    continue;
                }
                //判断设备是否在线
                boolean dataComplete = expressService.isDataComplete(express);
                if (dataComplete) {
                    onlineTag.add(tagNo);
                } else {
                    offlineTag.add(tagNo);
                }
            }
        }

        //订单总数
        result.put("expressCount", expressList.size() + "");
        result.put("alertExpressCount", alertExpressCount + "");
        result.put("handledAlertCount", handledAlertCount + "");
        result.put("unhandledAlertCount", unhandledAlertCount + "");
        result.put("allAlertCount", handledAlertCount + unhandledAlertCount + "");
        result.put("unhandledAlertExpressCount", unhandledAlertExpressCount + "");
        result.put("onlineTag", onlineTag.size() + "");
        result.put("offlineTag", offlineTag.size() + "");
        return result;
    }

    @RequestMapping("/getExpressList")
    @ResponseBody
    public Object getExpressList(int start, int length, String startTime, String endTime, String userName, boolean
            isCurrent) throws
            UnsupportedEncodingException {
        if (!validateUser()) {
            return DBUtils.getEmpty();
        }

        Map<String, Object> result = new HashMap<>();

        List<Express> expressList = expressService.getExpressList(lg.getUserId(), isCurrent, true, null, null,
                startTime, endTime, userName, false);

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
    }

    @RequestMapping("/ajaxEditExSleepTime")
    @ResponseBody
    public Object ajaxEditExSleepTime(int expressId, int time) {
        if (!validateUser()) {
            Response res = new Response(Response.ERROR);
            res.setMessage("无此权限！");
            return res;
        }
        Response res = expressService.editSleepTime(expressId, time);

        Utils.saveLog(lg.getUserId(), "设置订单上传周期");
        return res;
    }

    @RequestMapping("/editExpressAppointStart")
    @ResponseBody
    public Object editAppointStart(Integer expressId, String time) {
        if (!validateUser()) {
            Response res = new Response(Response.ERROR);
            res.setMessage("预约启动失败！");
            return res;
        }
        Response res = expressService.editAppointStart(expressId, time);

        Utils.saveLog(lg.getUserId(), "设置订单预约启动");
        return res;
    }

    @RequestMapping("/editExpressAppointEnd")
    @ResponseBody
    public Object editAppointEnd(Integer expressId, String time) {
        if (!validateUser()) {
            Response res = new Response(Response.ERROR);
            res.setMessage("预约结束失败！");
            return res;
        }
        Response res = expressService.editAppointEnd(expressId, time);

        Utils.saveLog(lg.getUserId(), "设置订单预约结束");
        return res;
    }

    @RequestMapping("/expressUsers")
    public String expressUsers(Model model, String id) {
        if (!validateUser()) {
            return "redirect:/";
        }
        Utils.saveLog(lg.getUserId(), "查看经手配送员");

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        model.addAttribute("id", id);
        return "express.express.expressUsers";
    }

    @RequestMapping("/getExpressUsers")
    @ResponseBody
    public Object getExpressUsers(Integer expressId, Model model, int draw) {
        // 查看订单经手配送员暂时没有做分页
        Map<String, Object> result = new HashMap<>();
        if (!validateUser() || null == expressId) {
            return null;
        }
        DbSession dbSession = BaseHibernateUtils.newSession();
        List<Map<String, Object>> list = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat userdateFormat = new SimpleDateFormat("yyyy年MM月dd日 ");
        try {
            List<UserExpress> userExpress = DBUtils.getUserExpressList(dbSession, expressId);
            Long recordsTotal = (Long) dbSession
                    .createQuery("select count(*) from UserExpress where expressId=:expressId and status=:status")
                    .setParameter("expressId", expressId).setParameter("status", Constants.State.STATE_FINISHED)
                    .uniqueResult();
            result.put("recordsTotal", recordsTotal);
            result.put("recordsFiltered", recordsTotal);
            for (UserExpress ue : userExpress) {
                // 根据id获取快递员
                Map<String, Object> map1 = new HashMap<String, Object>();
                User user = DBUtils.getUserById(dbSession, ue.getUserId());
                String domainName = (String) dbSession.createQuery("select name from Domain where id=:id")
                        .setParameter("id", user.getDomainId()).uniqueResult();
                map1.put("id", ue.getUserId());
                map1.put("staffNo", user.getStaffNo());
                map1.put("name", user.getName());
                map1.put("nickName", user.getNickName());
                map1.put("domainName", domainName);
                map1.put("gender", user.getGender());
                map1.put("mobile", user.getMobile());
                map1.put("birthDate", user.getBirthDate());
                map1.put("email", user.getEmail());
                map1.put("iconId", user.getIconId());
                map1.put("address", user.getAddress());
                map1.put("description", user.getDescription());
                map1.put("creationTime", dateFormat.format(ue.getCreationTime()));
                map1.put("lastModitied", dateFormat.format(ue.getLastModitied()));
                map1.put("userCreationTime", userdateFormat.format(user.getCreationTime()));
                list.add(map1);
            }

        } finally {
            dbSession.close();
        }
        result.put("draw", draw);
        result.put("data", list);
        return result;
    }

    @RequestMapping("/cancelSign")
    public String cancelSign(String expressNo, RedirectAttributes attr) {
        Utils.saveLog(lg.getUserId(), "撤销签收");
        Response res = expressService.cancelSign(lg, expressNo);

        attr.addFlashAttribute("message", res.getMessage());
        attr.addFlashAttribute("error", true);
        return "redirect:/history/1";
    }

    @RequestMapping("/exportTo/{flag}")
    public void exportTo(HttpServletRequest request, HttpServletResponse response, @PathVariable String flag) throws
            UnsupportedEncodingException {
        if (!validateUser()) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String expressIdStr = request.getParameter("expressId");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String fileType = request.getParameter("fileType");

        Date start = null;
        Date end = null;

        if (!StringUtils.isEmpty(startTimeStr)) {
            try {
                start = Utils.SF_NO_SECOND.parse(startTimeStr);
            } catch (ParseException e) {
                start = null;
            }
        }
        if (!StringUtils.isEmpty(endTimeStr)) {
            try {
                end = Utils.SF_NO_SECOND.parse(endTimeStr);
            } catch (ParseException e) {
                end = null;
            }
        }

        switch (fileType) {
            //PDF
            case "1":
                exportService.exportToPDF(Integer.parseInt(expressIdStr), start, end, Integer.parseInt(flag),
                        request, response);
                break;
            case "2":
                exportService.exportToXLS(Integer.parseInt(expressIdStr), start, end, Integer.parseInt(flag),
                        request, response);
                break;
            default:
                break;
        }

    }

}
