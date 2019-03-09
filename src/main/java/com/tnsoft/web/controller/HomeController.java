package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.*;
import com.tnsoft.web.model.*;
import com.tnsoft.web.service.*;
import com.tnsoft.web.servlet.ServletConsts;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.hibernate.SQLQuery;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class HomeController extends BaseController {

    @Resource(name = "homeService")
    private HomeService homeService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource(name = "alertService")
    private AlertService alertService;
    @Resource(name = "permissionService")
    private PermissionService perService;
    @Resource(name = "roleService")
    private RoleService roleService;
    @Resource(name = "tempExpressService")
    private TempExpressService tempExpressService;
    @Resource(name = "domainService")
    private DomainService domainService;

    private static final SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();


    @RequestMapping(value = "/timeout")
    public void sessionTimeout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getHeader("x-requested-with") != null
                && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
            response.getWriter().print("timeout");
            response.getWriter().close();
        } else {
            response.sendRedirect("login");
        }
    }

    @RequestMapping("/editMenu")
    public String editMenu() {
        if (!validateUser()) {
            return "view.login";
        }
        return "view.menu.editMenu";
    }

    @RequestMapping("/editRoleMenu")
    public ModelAndView editRoleMenu(Model model) {
        Utils.saveLog(lg.getUserId(), "编辑角色菜单");

        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }
        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            return new ModelAndView("redirect:/");
        }
        List<SelectItem> selectRol = new ArrayList<SelectItem>();
        //获得全部角色菜单关系,放入selectItem
        List<Role> roles = roleService.getAllRole();
        for (Role r : roles) {
            selectRol.add(new SelectItem(r.getId() + "", r.getName()));
        }
        model.addAttribute("roles", selectRol);
        return new ModelAndView("view.menu.editRoleMenu");

    }

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("files") MultipartFile[] files, RedirectAttributes attr)
            throws IllegalStateException {

        Response res = homeService.fileUpload(files, request);
        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", res.getMessage());
        return "redirect:/editMenu";
    }

    @RequestMapping("/savePreferences")
    public String savePreferences(String welcome, RedirectAttributes attr) {

        Response res = homeService.savePreferences(lg.getDomainId(), welcome);
        attr.addFlashAttribute("error", true);
        attr.addFlashAttribute("message", res.getMessage());
        return "redirect:/editMenu";
    }

    @RequestMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        return "view.login";
    }

    @RequestMapping("/help")
    public String help(Model model, HttpServletRequest request) {
        return "view.help.help";
    }

    @RequestMapping("/home")
    public String adminHome(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        LoginSession lg = (LoginSession) session.getAttribute(ServletConsts.ATTR_USER);
        if (lg == null) {
            model.addAttribute("name", session.getAttribute("remember_name"));
            model.addAttribute("pwd", session.getAttribute("remember_pwd"));
            return "view.login";
        }

        model.addAttribute("username", lg.getUserName());
        session.setAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return "view.home.home";
    }

    @RequestMapping("/query")
    public String query() {
        return "view.query";
    }

    @RequestMapping("/queryById")
    public String queryById(Model model, String expressId) {
        model.addAttribute("expressId", expressId);
        return "view.queryDetail";
    }

    @RequestMapping("/getMenus")
    @ResponseBody
    public List<List<Permission>> getMenus(Integer roleId, Integer pId, HttpServletRequest request) {
        // roleId不为空加载父菜单,pid不为空就是加载子菜单，menus[0]父菜单。menus[1]子菜单
        List<List<Permission>> menus = perService.getPermission(roleId);
        String path = request.getContextPath();
        // 获得本项目的地址(例如: http://localhost:8080/MyApp/)赋值给basePath
        String basePath = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + path;
        String currentMenu = "";
        int ignoreLen = 0;
        if (request.getServerPort() == 80) {
            //80时可省略端口号
            ignoreLen = (request.getServerPort() + ":").length();
        }
        if (request.getHeader("Referer") != null) {
            //上个页面的地址
            currentMenu = request.getHeader("Referer").substring(basePath.length() - ignoreLen);
        }
        List<Permission> current = new ArrayList<>();
        //menu[2]
        Permission currentMe = new Permission();
        currentMe.setUrl(currentMenu);
        current.add(currentMe);
        menus.add(current);
        return menus;
    }

    @RequestMapping("/ajaxQuery")
    public String ajaxQuery(Model model, String expressNo) {
        if (StringUtils.isEmpty(expressNo) || expressNo.contains("'")) {
            return "view.query";
        }
        DbSession db = BaseHibernateUtils.newSession();
        model.addAttribute("expressNo", expressNo);
        String pattern = ".*\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\)";
        try {
            String sql = "SELECT a.*, b.name AS domainName FROM nda_express a, nda_domain b WHERE b.id=a.domain_id " +
                    "AND a.express_no LIKE'%"
                    + expressNo + "%'";

            SQLQuery query = db.createSQLQuery(sql);
            query.addEntity(Express.class);
            query.addScalar("domainName", StringType.INSTANCE);
            List<?> list = query.list();
            if (!list.isEmpty()) {
                List<SelectItem> items = new ArrayList<>(list.size());
                List<String> expressNames = new ArrayList<>();
                List<String> expressIds = new ArrayList<>();
                for (Object o : list) {
                    Object[] row = (Object[]) o;
                    Express express = (Express) row[0];
                    if (!StringUtils.isEmpty(express.getExpressNo()) && Pattern.matches(pattern, express.getExpressNo())) {
                        if (expressNo.equals(express.getExpressNo().substring(0, (express.getExpressNo()).length() - 21))) {
                            expressNames.add("\"" + express.getExpressNo() + "(" + row[1] + ")" + "\"");
                            expressIds.add(String.valueOf(express.getId()));
                        }
                    } else {
                        if (expressNo.equals(express.getExpressNo())) {
                            expressNames.add("\"" + express.getExpressNo() + "(" + row[1] + ")" + "\"");
                            expressIds.add(String.valueOf(express.getId()));
                        }
                    }
                }
                model.addAttribute("expressenames", expressNames);
                model.addAttribute("expresseids", expressIds);
            }
        } finally {
            db.close();
        }

        return "view.queryLists";
    }

    @RequestMapping("/ajaxQueryExpress")
    @ResponseBody
    public void ajaxQueryExpress(String expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();

        Result result = new Result(Result.OK);

        List<TempItem> temps = new ArrayList<>();

        try {

            Express express = (Express) db.get(Express.class, Integer.parseInt(expressId));
            if (express.getCheckInTime() != null) {
                result.setBegin("配送开始:" + Utils.SF.format(express.getCheckInTime()));
            } else {
                result.setBegin("未开始配送");
            }

            if (express.getCheckOutTime() != null) {
                result.setEnd("已签收:" + Utils.SF.format(express.getCheckOutTime()));
            } else {
                result.setEnd("未签收");
            }

            List<TempExpress> list = DBUtils.getAllTempesByExpressIdDesc(db, express.getId());
            if (!list.isEmpty()) {
                for (TempExpress ne : list) {
                    temps.add(new TempItem(String.format("%.2f", ne.getTemperature()),
                            String.format("%.2f", ne.getHumidity()), Utils.SF.format(ne.getCreationTime())));
                }
            }
        } finally {
            db.close();
        }
        result.setTemps(temps);

        out.write(Utils.GSON.toJson(result));
    }

    @RequestMapping("/guestAjaxTemp")
    @ResponseBody
    public void guestAjaxTemp(String expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        DbSession db = BaseHibernateUtils.newSession();
        Result result = new Result();
        List<String> time = new ArrayList<>();
        List<String> temperature = new ArrayList<>();
        List<String> humidity = new ArrayList<>();
        try {

            if (expressId != null) {
                List<TempExpress> list = DBUtils.getAllTempesByExpressId(db, Integer.parseInt(expressId));
                if (!list.isEmpty()) {
                    for (TempExpress ndaTempExpress : list) {
                        time.add(SF.format(ndaTempExpress.getCreationTime()));
                        temperature.add(String.format("%.2f", ndaTempExpress.getTemperature()));
                        humidity.add(String.format("%.2f", ndaTempExpress.getHumidity()));
                    }
                }
            }
        } finally {
            db.close();
        }

        result.setTime(time);
        result.setTemperature(temperature);
        result.setHumidity(humidity);

        if (temperature.isEmpty() || humidity.isEmpty()) {
            result.setCode(Result.ERROR);
        }
        out.write(GSON.toJson(result));
    }

    @RequestMapping("/getDomainPreferences")
    @ResponseBody
    public Object getDomainPreferences() {
        String preferences = domainService.getPreferencesByDomainId(lg.getDomainId());
        Map<String, String> ans = new HashMap<>();
        ans.put("welcome", preferences);
        return ans;
    }

    @RequestMapping("/guestGetTagBreifInfo")
    @ResponseBody
    public Object guestGetTagBreifInfo(String tagNo) {
        Tag tag = tagService.getById(tagNo);
        Map<String, Object> map = Utils.ObjToMap(tag);
        // 还需要加入报警相关信息,为了以后方便,直接获得报警对象
        List<NDAAlert> list = alertService.getAlertByTagNo(tagNo);
        // 暂时只讲报警的次数放入到map里
        map.put("alertCount", list.size());
        return map;
    }

    @RequestMapping("/guestGetExpressBreifInfo")
    @ResponseBody
    public void guestGetExpressBreifInfo(String expressId, HttpServletResponse resp) throws IOException {
        resp.setContentType(ServletConsts.CONTENT_TYPE_JSON);
        PrintWriter out = resp.getWriter();
        if (StringUtils.isEmpty(expressId)) {
            return;
        }
        Map<String, String> result = tempExpressService.getBriefInfoByExpressId(Integer.parseInt(expressId));
        out.write(GSON.toJson(result));
    }

}
