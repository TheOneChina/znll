package com.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.OperateLog;
import com.tnsoft.web.service.OperateLogService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

@Controller
public class LogController extends BaseController {

    @Resource()
    private OperateLogService operateLogService;

    public LogController() {
        super();
    }

    @RequestMapping("/logs/{flag}")
    public String logs(Model model, @PathVariable String flag) {

        if (!validateUser()) {
            return "redirect:/";
        }

        String view = "view.log.logs";
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return view;
    }

    @RequestMapping("/getLogs")
    @ResponseBody
    public Object getLogs(int start, int length, String startTime, String endTime, String userName) {
        if (!validateUser()) {
            return DBUtils.getEmpty();
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

        List<OperateLog> logList = operateLogService.getLogs(lg.getUserId(), true, null, null, startDate, endDate);

        if (!StringUtils.isEmpty(userName)) {
            List<OperateLog> logs = new ArrayList<>();
            for (OperateLog log : logList) {
                if (log.getUserName().contains(userName)) {
                    logs.add(log);
                }
            }
            logList = logs;
        }

        if (null == logList || logList.size() < 1) {
            return DBUtils.getEmpty();
        }
        //查询到的数据总条数
        result.put("recordsTotal", logList.size());


        Map<String, String[]> properties = request.getParameterMap();
        String search = "";
        if (null != properties.get("search[value]")) {
            search = properties.get("search[value]")[0];
        }
        if (search.contains("'")) {
            search = "";
        }

        if (!StringUtils.isEmpty(search)) {
            List<OperateLog> logs = new ArrayList<>();
            for (OperateLog log : logList) {
                if (null != log.getOperation() && log.getOperation().contains(search)) {
                    logs.add(log);
                }
            }
            logList = logs;
        }

        //搜索后查到的数据条数
        if (logList.size() < 1) {
            result.put("recordsFiltered", 0);
            result.put("data", Collections.emptyList());
            return result;
        }
        result.put("recordsFiltered", logList.size());


        if (start > logList.size()) {
            result.put("data", Collections.emptyList());
        } else {
            int endIdx;
            if (start + length > logList.size()) {
                endIdx = logList.size();
            } else {
                endIdx = start + length;
            }
            logList = logList.subList(start, endIdx);
            result.put("data", logList);
        }

        return result;
    }

}
