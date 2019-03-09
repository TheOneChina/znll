package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.Calibration;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Response;
import com.tnsoft.web.service.CalibrationService;
import com.tnsoft.web.service.ExportService;
import com.tnsoft.web.service.TagService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
public class CalibrationController extends BaseController {

    @Resource(name = "calibrationService")
    private CalibrationService calibrationService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource(name = "exportService")
    private ExportService exportService;

    @RequestMapping("/queryCalibration")
    public String queryCalibration(Model model) {
        Utils.saveLog(lg.getUserId(), "查看模块校准");
        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return "tag.super.queryCalibration";
    }

    @RequestMapping("/calibrate")
    public String calibrate(Model model) {
        Utils.saveLog(lg.getUserId(), "模块校准");
        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return "tag.super.calibrate";
    }

    @RequestMapping("/createTask")
    public String createTask(Model model) {
        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        return "tag.super.createTask";
    }

    @RequestMapping("/calibrateUploadByTagNo")
    public String calibrateUploadByTagNo(Model model, String tagNo, String id) {
        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        model.addAttribute("tagNo", tagNo);
        model.addAttribute("calibrateId", id);
        return "tag.super.calibrateByTag";
    }

    @RequestMapping("/saveCreateTask")
    public String saveCreateTask(Model model, String tagNos, String taskName, RedirectAttributes attr) {
        if (!validateUser()) {
            return "redirect:/";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "redirect:/";
        }
        attr.addFlashAttribute("error", true);

        if (StringUtils.isEmpty(tagNos)) {
            attr.addFlashAttribute("message", "设备号为空！");
            return "redirect:/calibrate";
        }
        String[] tagNoArr = tagNos.split(";");
        if (tagNoArr.length < 1) {
            attr.addFlashAttribute("message", "设备号为空！");
            return "redirect:/calibrate";
        }
        Set<String> set = new HashSet<>();
        for (String tagNo : tagNoArr) {
            if (!StringUtils.isEmpty(tagNo)) {
                set.add(tagNo.trim());
            }
        }
        boolean result = calibrationService.createCalibrationTask(taskName, set);
        if (result) {
            attr.addFlashAttribute("message", "校准任务创建成功");
        } else {
            attr.addFlashAttribute("message", "校准任务创建失败");
        }
        Utils.saveLog(lg.getUserId(), "创建校准任务");
        return "redirect:/calibrate";
    }

    @RequestMapping("/getCalibrations")
    @ResponseBody
    public Object getCalibrations() {
        if (!validateUser()) {
            return "";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "";
        }
        List<Calibration> list = calibrationService.getAll();
        Map<String, Object> ans = new HashMap<>();
        ans.put("data", list);
        return ans;
    }

    @RequestMapping("/calibrateByTags")
    @ResponseBody
    public Object calibrateByTags(int start, int length, String tagNo, String calibrateId) {
        if (!validateUser()) {
            return "";
        }
        if (lg.getRoles().get(0).getId() != Constants.Role.SUPER_ADMIN) {
            return "";
        }

        return calibrationService.getTagCalibrationUpload(start, length, tagNo, Integer.parseInt(calibrateId));
    }

    @RequestMapping("/saveAndExportCalibration")
    public void saveAndExportCalibration(HttpServletRequest request, HttpServletResponse response, String tagNo, int
            calibrationType, float standardLowTemp, float lowTemp, float standardMediumTemp, float mediumTemp, float
                                                 standardHighTemp, float highTemp, float standardHumidity, float
                                                 humidity) throws
            UnsupportedEncodingException {
        if (!validateUser()) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        tagService.saveCalibration(tagNo, calibrationType, standardLowTemp, lowTemp, standardMediumTemp, mediumTemp,
                standardHighTemp, highTemp, standardHumidity, humidity);

        exportService.exportCalibrationToPDF(tagNo, request, response);

    }


    @RequestMapping("/getCalibrateTags")
    @ResponseBody
    public Object getCalibrateTags(int id) {
        return calibrationService.getTagsByCalibrationId(id);
    }

    @RequestMapping("/saveStandardTempAndHumidity")
    @ResponseBody
    public Object saveStandardTempAndHumidity(int id, int flag, Float temp, Float humidity) {
        Response response = new Response(Response.ERROR);
        if (!validateUser()) {
            return response;
        }
        boolean result = calibrationService.saveStandardValue(id, flag, temp, humidity);
        if (result) {
            response.setCode(Response.OK);
        }
        return response;
    }

    @RequestMapping("/nextTaskStep")
    public String editBuzzer(int id, RedirectAttributes attr) {
        if (!validateUser()) {
            return "view.login";
        }

        boolean result = calibrationService.nextTaskStep(id);
        if (result) {
            attr.addFlashAttribute("message", "成功");
        } else {
            attr.addFlashAttribute("message", "失败");
        }

        attr.addFlashAttribute("error", true);

        return "redirect:/calibrate";
    }

}

