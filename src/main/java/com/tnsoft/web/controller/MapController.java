package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.model.LocateExpress;
import com.tnsoft.web.service.MapService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class MapController extends BaseController {

    @Resource(name = "mapService")
    private MapService mapService;

    public MapController() {
        super();
    }

    @RequestMapping("/map")
    public String map(Model model, String id) {
        Utils.saveLog(lg.getUserId(), "查看订单位置信息");
        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());

        return "view.map.map";
    }


    @RequestMapping("/ajaxLocation")
    @ResponseBody
    public Object ajaxLocation(String id, String model) {

        if (!StringUtils.isEmpty(id)) {

            List<LocateExpress> list = mapService.getLocationByExpressId(Integer.parseInt(id));

            for (LocateExpress locateExpress : list) {
                locateExpress.setTime(Utils.SF.format(locateExpress.getCreationTime()));
            }

            return list;
        }
        return null;
    }
}
