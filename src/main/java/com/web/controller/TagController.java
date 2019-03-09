package com.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.hibernate.model.Express;
import com.tnsoft.hibernate.model.Tag;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.DomainService;
import com.tnsoft.web.service.ExpressService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.service.UserService;
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
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 设备相关业务的Controller
 *
 * @author z
 */
@Controller
public class TagController extends BaseController {

    @Resource()
    private TagService tagService;
    @Resource()
    private ExpressService expressService;
    @Resource()
    private DomainService domainService;
    @Resource()
    private UserService userService;

    public TagController() {
        super();
    }

    @RequestMapping("/tags/{flag}")
    public String tags(Model model, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "查看模块列表");
        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "0":
                view = "tag.super.tags";
                break;
            case "1":
                view = "tag.express.tags";
                break;
            case "2":
                view = "tag.medicine.tags";
                break;
            case "3":
                view = "tag.standard.tags";
                break;
            default:
        }
        return view;
    }

    /**
     * 打包下载所有Bin文件
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/exportBinZip")
    public void exportBinZip(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUser()) {
            return;
        }
        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        //IO流实现下载的功能
        response.setContentType("application/x-msdownload");
        //获取文件名称（包括文件格式）
        String fileName = "bin.zip";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        OutputStream out = response.getOutputStream();

        //获取文件根目录，不同框架获取的方式不一样，可自由切换
        String basePath = request.getSession().getServletContext().getRealPath("/WEB-INF");

        String dirPath = basePath + File.separator + "binDir" + File.separator;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<Tag> tags = tagService.getAll();
        for (Tag ndaTag : tags) {
            String tmp = ndaTag.getTagNo();
            byte[] arr = new byte[128];
            byte[] t = StringUtils.toBytesQuietly(tmp);
            System.arraycopy(t, 0, arr, 0, t.length);
            FileOutputStream fop = null;
            String name = ndaTag.getName();
            String path = null;
            if (name != null && name.length() > 0) {
                path = dirPath + ndaTag.getName() + ".bin";
            } else {
                path = dirPath + ndaTag.getTagNo() + ".bin";
            }
            try {
                File file = new File(path);
                fop = new FileOutputStream(file);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fop.write(arr, 0, arr.length);
                fop.flush();
                fop.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fop != null) {
                    try {
                        fop.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("bin File Done! ");

        //创建压缩文件需要的空的zip包
        String zipFilePath = basePath + File.separator + "bin.zip";

        //创建文件路径的集合，
        List<String> filePath = new ArrayList<>();
        filePath.add(dirPath);

        //根据临时的zip压缩包路径，创建zip文件
        File zip = new File(zipFilePath);
        if (!zip.exists()) {
            zip.createNewFile();
        }

        //创建zip文件输出流
        FileOutputStream fos = new FileOutputStream(zip);
        ZipOutputStream zos = new ZipOutputStream(fos);

        //循环读取文件路径集合，获取每一个文件的路径
        for (String fp : filePath) {
            File f = new File(fp);
            zipFile(f, zos);
        }
        zos.close();
        fos.close();

        //将打包后的文件写到客户端，输出的方法同上，使用缓冲流输出
        InputStream fis = new BufferedInputStream(new FileInputStream(zipFilePath));
        byte[] buff = new byte[4096];
        int size = 0;
        while ((size = fis.read(buff)) != -1) {
            out.write(buff, 0, size);
        }
    }

    /**
     * 封装压缩文件的方法
     *
     * @param inputFile
     * @param zipoutputStream
     */
    public void zipFile(File inputFile, ZipOutputStream zipoutputStream) {
        try {
            if (inputFile.exists()) {
                if (inputFile.isFile()) {
                    //创建输入流读取文件
                    FileInputStream fis = new FileInputStream(inputFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    //将文件写入zip内，即将文件进行打包
                    ZipEntry ze = new ZipEntry(inputFile.getName());
                    zipoutputStream.putNextEntry(ze);

                    //写入文件的方法，同上
                    byte[] b = new byte[1024];
                    long l = 0;
                    while (l < inputFile.length()) {
                        int j = bis.read(b, 0, 1024);
                        l += j;
                        zipoutputStream.write(b, 0, j);
                    }
                    //关闭输入输出流
                    bis.close();
                    fis.close();
                } else {  //如果是文件夹，则使用穷举的方法获取文件，写入zip
                    try {
                        File[] files = inputFile.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            zipFile(files[i], zipoutputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping("/getTagByTagNo")
    @ResponseBody
    public Response getTagByTagNo(String tagNo) {
        Response response = new Response(Response.ERROR);
        if (!validateUser()) {
            return response;
        }

        Tag tag = tagService.getById(tagNo);
        if (null == tag) {
            return response;
        }

        response.setTag(tag);
        response.setCode(Response.OK);
        return response;
    }

    @RequestMapping("/getDomainNameByTagNo")
    @ResponseBody
    public Response getDomainNameByTagNo(String tagNo) {
        Response response = new Response(Response.ERROR);
        if (!validateUser()) {
            return response;
        }

        Tag tag = tagService.getById(tagNo);
        if (null == tag) {
            return response;
        }
        response.setCode(Response.OK);
        if (tag.getDomainId() == null) {
            response.setMessage("无用户信息。");
        } else {
            Domain domain = domainService.getById(tag.getDomainId());
            if (null == domain || domain.getName() == null) {
                response.setMessage("无用户信息。");
            } else {
                User currentUser = tagService.getCurrentUserByTagNo(tagNo);
                if (null == currentUser) {
                    response.setMessage("所属用户：" + domain.getName());
                } else {
                    response.setMessage("所属用户：" + domain.getName() + "\n使用者：" + currentUser.getName());
                }
            }
        }
        return response;
    }


    @RequestMapping("/getTagByExpressId")
    @ResponseBody
    public Tag getTagByExpressId(int expressId) {
        if (!validateUser()) {
            return null;
        }
        String tagNo = expressService.getTagNoByExpressId(expressId);
        return tagService.getById(tagNo);
    }


    @RequestMapping("/APConfig/{flag}")
    public ModelAndView APConfig(String tagNo, Model model, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "设置AP");

        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }
        // 获得tag
        Tag tag = tagService.getById(tagNo);
        model.addAttribute("tagNo", tagNo);

        String view = "";
        switch (flag) {
            case "0":
                view = "tag.super.apConfig";
                break;
            case "1":
                view = "tag.express.apConfig";
                break;
            case "2":
                view = "tag.medicine.apConfig";
                break;
            case "3":
                view = "tag.standard.apConfig";
                break;
            default:
        }

        return new ModelAndView(view, "command", tag);

    }

    @RequestMapping("/saveTagAPConfig/{flag}")
    public String saveTagAPConfig(String SSID, String password, String tagNo, Model model, RedirectAttributes attr,
                                  @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "保存AP密码");

        if (!validateUser()) {
            return "redirect:/";
        }
        if (StringUtils.isEmpty(SSID) || StringUtils.isEmpty(password)) {
            attr.addFlashAttribute("message", "数据不能为空！");
        } else if (!Utils.isValidAPName(SSID)) {
            attr.addFlashAttribute("message", "AP名应为字母数字组合，不低于4位！");
        } else if (!Utils.isValidAPPasswd(password)) {
            attr.addFlashAttribute("message", "AP密码应为字母数字组合，不低于8位！");
        } else {
            Response res = tagService.saveTagAPConfig(SSID, password, tagNo);
            if (res.getCode() == 0) {
                attr.addFlashAttribute("message", "设置成功");
            } else {
                attr.addFlashAttribute("message", "设置失败");
            }
        }
        attr.addFlashAttribute("error", true);
        return "redirect:/tags/" + flag;
    }

    @RequestMapping("/editTag/{flag}")
    public ModelAndView editTag(Model model, String[] tagNos, HttpSession session, @PathVariable String flag) {
        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }
        // 传过来的是list
        session.setAttribute("tagNos", tagNos);
        Tag tag;
        if (null != tagNos && tagNos.length == 1) {
            tag = tagService.getById(tagNos[0]);
        } else {
            tag = new Tag();
        }
        String view = "";
        switch (flag) {
            case "0":
                view = "tag.super.editTag";
                break;
            case "1":
                view = "tag.express.editTag";
                break;
            case "2":
                view = "tag.medicine.editTag";
                break;
            case "3":
                view = "tag.standard.editTag";
                break;
            default:
        }
        return new ModelAndView(view, "command", tag);
    }

    /**
     * 批量设置设备参数
     *
     * @param temperatureMin 温度下限
     * @param temperatureMax 温度上限
     * @param sleepTime      上传周期
     * @param buzzer         蜂鸣器
     * @param appointStart   预约开始
     * @param alertPhones    报警手机号码
     * @param session        HTTPSession
     * @param attr           attr
     * @param flag           版本标识
     * @return 重定向至设备列表页面
     */
    @RequestMapping("/saveEditTag/{flag}")
    public String saveEditTag(String temperatureMin, String temperatureMax, String sleepTime, Integer buzzer, String
            appointStart, String alertPhones, HttpSession session,
                              RedirectAttributes attr, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "批量设置设备");
        String[] tagNos = (String[]) session.getAttribute("tagNos");
        Integer appointStartInt = null;
        if (null != appointStart && !appointStart.isEmpty()) {
            appointStartInt = Integer.parseInt(appointStart);
        }
        Response res = tagService.editTag(tagNos, null, null, temperatureMin, temperatureMax, sleepTime, buzzer,
                appointStartInt, alertPhones, Integer.parseInt(flag));

        attr.addFlashAttribute("message", res.getMessage());
        attr.addFlashAttribute("error", true);
        return "redirect:/tags/" + flag;
    }

    @RequestMapping("/tagTemplate")
    public String tagTemplate(String[] tagNos, Integer model, RedirectAttributes attr) {
        Utils.saveLog(lg.getUserId(), "使用场景模板");
        Response res = tagService.tagTemplate(tagNos, model);

        attr.addFlashAttribute("message", res.getMessage());
        attr.addFlashAttribute("error", true);
        return "redirect:/tags/1";
    }

    @RequestMapping("/temperature/{flag}")
    public ModelAndView temperature(Model model, String id, @PathVariable String flag) {
        Utils.saveLog(lg.getUserId(), "查看模块温度记录");
        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }
        Tag tag;

        if (!StringUtils.isEmpty(id)) {
            model.addAttribute("id", id);
            tag = tagService.getById(id);
        } else {
            tag = new Tag();
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        String view = "";
        switch (flag) {
            case "0":
                view = "tag.super.temperature";
                break;
            case "1":
                view = "tag.express.temperature";
                break;
            case "2":
                view = "tag.medicine.temperature";
                break;
            case "3":
                view = "tag.standard.temperature";
                break;
            default:
        }
        return new ModelAndView(view, "command", tag);
    }

    @RequestMapping("/saveTemperature/{flag}")
    public String saveTemperature(Model model, String tagNo, String temperatureMin, String temperatureMax,
                                  RedirectAttributes attr, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        Float lowValue = null;
        Float highValue = null;
        if (!StringUtils.isEmpty(temperatureMin)) {
            lowValue = Float.parseFloat(temperatureMin);
        }
        if (!StringUtils.isEmpty(temperatureMax)) {
            highValue = Float.parseFloat(temperatureMax);
        }
        Response response = tagService.editTemperature(tagNo, lowValue, highValue);

        attr.addFlashAttribute("error", true);
        if (response.getCode() == Response.OK) {
            attr.addFlashAttribute("message", "模块温度设置成功");
        } else {
            attr.addFlashAttribute("message", "模块温度设置失败");
        }
        Utils.saveLog(lg.getUserId(), "设置模块温度");
        return "redirect:/tags/" + flag;
    }


    /**
     * 新增设备，超级管理员可用
     *
     * @param model        缺省
     * @param times        新增设备数量
     * @param hardwareType 新增设备类型
     * @return 返回结果
     */
    @RequestMapping("/createTag")
    @ResponseBody
    public Object createTag(Model model, Integer times, int hardwareType) {
        Response resp = new Response(Response.ERROR);
        if (!validateUser()) {
            resp.setMessage("新增失败！");
            return resp;
        }
        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            resp.setMessage("无权新增！");
            return resp;
        }
        if (null == times || times < 1) {
            resp.setMessage("请输入正确的数量！");
            return resp;
        }

        boolean result = tagService.createNewTags(times, hardwareType);
        if (result) {
            resp.setCode(0);
            resp.setMessage("新增成功");
        } else {
            resp.setMessage("新增失败");
        }

        Utils.saveLog(lg.getUserId(), "新增模块");

        return resp;
    }

    @RequestMapping("/addSMS")
    @ResponseBody
    public Object addSMS(Model model, int num, String tagNo) {
        Response resp = new Response(Response.ERROR);
        if (!validateUser()) {
            return resp;
        }
        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            resp.setMessage("无此权限！");
            return resp;
        }
        if (num < 1 || null == tagNo) {
            resp.setMessage("请输入正确的值！");
            return resp;
        }
        String[] tagNos = {tagNo};
        resp = tagService.addSMS(tagNos, num);

        Utils.saveLog(lg.getUserId(), "增加短信数");

        return resp;
    }

    @RequestMapping("/addServiceTime")
    public Object addServiceTime(String tagNo, RedirectAttributes attr) {

        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        if (null == tagNo) {
            return "redirect:/";
        }
        String[] tagNos = {tagNo};
        Response resp = tagService.addServiceTime(tagNos, 1);
        Utils.saveLog(lg.getUserId(), "增加云平台使用时间");
        attr.addFlashAttribute("message", resp.getMessage());
        attr.addFlashAttribute("error", true);
        return "redirect:/tags/0";
    }

    /**
     * 添加设备到相应的账户
     *
     * @param tagNo 设备识别码
     * @return Response消息体
     */
    @RequestMapping("/scanTag")
    @ResponseBody
    public Response scanTag(String tagNo) {
        Utils.saveLog(lg.getUserId(), "添加设备");
        if (!validateUser()) {
            return new Response(1, "权限不足");
        }
        return tagService.scanTag(tagNo, lg.getRootDomainId());
    }


    @RequestMapping("/editBuzzer/{flag}")
    public String editBuzzer(String tagNo, int mode, RedirectAttributes attr, @PathVariable String flag) {
        if (!validateUser()) {
            return "view.login";
        }
        // 该参数model不是modelAndView中的Model,是蜂鸣器的关闭模式还是开启模式
        Response res = tagService.editBuzzer(tagNo, mode);
        attr.addFlashAttribute("message", res.getMessage());
        attr.addFlashAttribute("error", true);

        return "redirect:/tags/" + flag;
    }

    @RequestMapping("/editBuzzerByExpress/{flag}")
    public String editBuzzerByExpress(String tagNo, int mode, RedirectAttributes attr, @PathVariable String flag) {
        if (!validateUser()) {
            return "view.login";
        }
        Response res = tagService.editBuzzer(tagNo, mode);
        attr.addFlashAttribute("message", res.getMessage());
        attr.addFlashAttribute("error", true);

        return "redirect:/express/" + flag;
    }

    @RequestMapping("/editTagTemperature")
    @ResponseBody
    public Object editTagTemperature(String tagNo, Float max, Float min) {
        if (!validateUser()) {
            Response resp = new Response(Response.ERROR);
            resp.setMessage("无此权限！");
            return resp;
        }
        Response resp = tagService.editTemperature(tagNo, min, max);
        Utils.saveLog(lg.getUserId(), "设置模块温度上下限");
        return resp;
    }

    @RequestMapping("/editAppointStart")
    @ResponseBody
    public Object editAppointStart(String id, String time) {
        if (!validateUser()) {
            return "view.login";
        }

        return tagService.editAppointStart(id, time);
    }

    @RequestMapping("/deleteTag/{flag}")
    public String deleteTag(String tagNo, RedirectAttributes attr, @PathVariable String flag) {

        if (!validateUser()) {
            return "redirect:/";
        }

        Response response = tagService.deleteTag(tagNo, lg.getUserId());

        if (response.getCode() == Response.OK) {
            attr.addFlashAttribute("message", "模块删除成功");
        } else {
            attr.addFlashAttribute("message", "模块删除失败");
        }
        attr.addFlashAttribute("error", true);
        return "redirect:/tags/" + flag;
    }

    @RequestMapping("/setTagStatus/{flag}")
    public String setTagStatus(String id, int mode, RedirectAttributes attr, @PathVariable String flag) {

        if (!validateUser()) {
            return "redirect:/";
        }
        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();
            Date now = new Date();
            if (!StringUtils.isEmpty(id)) {
                Tag tag = (Tag) db.get(Tag.class, id);
                if (tag != null) {
                    tag.setLastModitied(now);
                    if (mode == 1) {
                        attr.addFlashAttribute("message", "模块禁用成功");
                        Utils.saveLog(lg.getUserId(), "禁用模块");
                        tag.setStatus(Constants.TagState.STATE_DELETE);
                    } else {
                        attr.addFlashAttribute("message", "模块启用成功");
                        Utils.saveLog(lg.getUserId(), "启用模块");
                        tag.setStatus(Constants.TagState.STATE_ACTIVE);
                        if (DBUtils.hasBind(db, tag.getTagNo())) {
                            tag.setStatus(Constants.TagState.STATE_WORKING);
                        }
                    }
                }
            }
            db.commit();
        } finally {
            db.close();
        }

        attr.addFlashAttribute("error", true);

        return "redirect:/tags/" + flag;
    }

    @RequestMapping("/tagHistory/{flag}")
    public String tagHistory(Model model, @PathVariable String flag) {

        if (!validateUser()) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName());

        String view = "";
        switch (flag) {
            case "1":
                view = "tag.express.history";
                break;
            case "2":
                view = "tag.medicine.history";
                break;
            case "3":
                view = "tag.standard.history";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/ajaxEditSleepTime")
    @ResponseBody
    public Object ajaxEditSleepTime(String id, String time) {
        if (!validateUser()) {
            Response resp = new Response(Response.ERROR);
            resp.setMessage("无此权限！");
            return resp;
        }

        Response resp = tagService.editSleepTime(id, Integer.parseInt(time));
        if (resp.getCode() == Response.OK) {
            resp.setMessage("设置成功！");
        } else {
            resp.setMessage("设置失败！");
        }
        Utils.saveLog(lg.getUserId(), "设置模块睡眠时间");
        return resp;
    }

//    @RequestMapping("/ajaxEditName")
//    @ResponseBody
//    public Object ajaxEditName(String id, String name) {
//        if (!validateUser()) {
//            Response resp = new Response(Response.ERROR);
//            resp.setMessage("模块名称编辑失败！");
//            return resp;
//        }
//
//        Response resp = tagService.editName(id, name);
//        if (resp.getCode() == Response.OK) {
//            resp.setMessage("模块名称编辑成功！");
//            Utils.saveLog(lg.getUserId(), "编辑模块名称");
//        } else {
//            resp.setMessage("模块名称编辑失败！");
//        }
//        return resp;
//    }

    @RequestMapping("/ajaxTags")
    @ResponseBody
    public Object ajaxTags(int draw, int start, int length, String search) {
        if (!validateUser()) {
            return "";
        }
        return query(draw, start, length, search, " order by a.name ASC ");
    }

    private Map<String, Object> query(int draw, int start, int length, String userName, String defaultOrderBy) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String[]> properties = request.getParameterMap();
        String orderSql;
        if (null == defaultOrderBy || "".equals(defaultOrderBy)) {
            orderSql = "ORDER BY a.name ASC ";
        } else {
            orderSql = defaultOrderBy;
        }

        String whereClause = "";

        if (!StringUtils.isEmpty(userName)) {
            User user = userService.getUSerByUserName(userName);
            if (null != user) {
                whereClause = " a.domainId=" + user.getRootDomainId();
            } else {
                whereClause = " a.domainId=-1";
            }
        }

        if (lg.getDefRole().getId() != Constants.Role.SUPER_ADMIN) {
            whereClause = " a.domainId=" + lg.getRootDomainId();
        }

        String search = properties.get("search[value]")[0];

        long recordsFiltered;
        long recordsTotal = count(whereClause);
        result.put("recordsTotal", recordsTotal);

        if (search.contains("'")) {
            search = "";
        }
        if (!StringUtils.isEmpty(search)) {
            if (!StringUtils.isEmpty(whereClause)) {
                whereClause += " AND ";
            }
            whereClause += " (a.tagNo LIKE '%" + search + "%' OR a.name LIKE '%" + search + "%') ";
        }

        String status = properties.get("columns[5][search][value]")[0];
        if (!StringUtils.isEmpty(status)) {
            if (!StringUtils.isEmpty(whereClause)) {
                whereClause += " AND ";
            }
            whereClause += " a.status=" + status + " ";
        }

        String sms = properties.get("columns[7][search][value]")[0];
        if (!StringUtils.isEmpty(sms)) {
            if (!StringUtils.isEmpty(whereClause)) {
                whereClause += " AND ";
            }
            switch (sms) {
                case "0":
                    whereClause += " a.sms=0 ";
                    break;
                case "1":
                    whereClause += " a.sms<30 ";
                    break;
                case "2":
                    whereClause += " a.sms>=30 ";
                    break;
                default:
                    break;
            }
        }

        String day = properties.get("columns[8][search][value]")[0];
        if (!StringUtils.isEmpty(day)) {
            if (!StringUtils.isEmpty(whereClause)) {
                whereClause += " AND ";
            }
            Date now = new Date();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            long temp = 24 * 3600 * 1000;
            switch (day) {
                case "0":
                    temp = temp * 365;
                    now.setTime(now.getTime() - temp);
                    whereClause += " a.calibrationTime<=\'" + sf.format(now) + "\' ";
                    break;
                case "1":
                    temp = temp * 335;
                    now.setTime(now.getTime() - temp);
                    whereClause += " a.calibrationTime<=\'" + sf.format(now) + "\' ";
                    break;
                case "2":
                    temp = temp * 335;
                    now.setTime(now.getTime() - temp);
                    whereClause += " a.calibrationTime>\'" + sf.format(now) + "\' ";
                    break;
                default:
                    break;
            }
        }

        if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status) || !StringUtils.isEmpty(sms) || !StringUtils.isEmpty(day)) {
            recordsFiltered = count(whereClause);
        } else {
            recordsFiltered = recordsTotal;
        }
        result.put("recordsFiltered", recordsFiltered);

        result.put("data", query(whereClause, orderSql, start, length));
        return result;

    }


    private int count(String where) {

        String sql = "from Tag a ";

        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }
        return tagService.count(sql);
    }

    private List<Tag> query(String where, String order, int offset, int limit) {

        String sql = "from Tag a ";

        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        if (!StringUtils.isEmpty(order)) {
            sql += (" " + order);
        }

        List<Tag> list = tagService.getByHQLWithLimits(offset, limit, sql);
        if (null != list && !list.isEmpty()) {
            for (Tag e : list) {
                if (e.getStatus() == Constants.TagState.STATE_ACTIVE) {
                    e.setStatusName("启用");
                } else if (e.getStatus() == Constants.TagState.STATE_WORKING) {
                    e.setStatusName("工作中");
                } else if (e.getStatus() == Constants.TagState.STATE_DELETE) {
                    e.setStatusName("禁用");
                }
                if (e.getElectricity() == null) {
                    e.setElectricityStatus("未知");
                } else {
                    if (e.getElectricity() == 1 || e.getElectricity() >= Constants.Electricity.NORMAL) {
                        e.setElectricityStatus("<font color='green'>充足</font>");
                    } else {
                        e.setElectricityStatus("<font color='red'>不足</font>");
                    }
                }
            }
        }
        return list;
    }


    @RequestMapping("/getTagExpresses")
    @ResponseBody
    public Response getTagExpressHistory(String tagNo) {
        Response res = new Response();
        List<Express> list = tagService.getAllExpresses(tagNo);
        if (list.size() > 0) {
            res.setCode(Response.OK);
        } else {
            res.setCode(Response.ERROR);
        }
        res.setExpress(list);
        return res;
    }

}
