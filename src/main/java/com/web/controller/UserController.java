package com.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Role;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.model.Result;
import com.tnsoft.web.model.SelectItem;
import com.tnsoft.web.service.UserService;
import com.tnsoft.web.util.DBUtils;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class UserController extends BaseController {

    @Resource(name = "userService")
    private UserService userService;

    @RequestMapping("/user")
    public String user(Model model) {
        Utils.saveLog(lg.getUserId(), "查看人员列表");
        if (!validateUser()) {
            return "redirect:/";
        }

        // 参数返回到界面上
        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        model.addAttribute("roleId", lg.getDefRole().getId());
        return "view.user.user";
    }

    @RequestMapping("/editUser")
    public ModelAndView editUser(Model model, String mode, String id) {

        if (!validateUser()) {
            return new ModelAndView("redirect:/");
        }
        User user;
        if ("edit".equals(mode) && !StringUtils.isEmpty(id)) {
            user = userService.getById(Integer.parseInt(id));
            model.addAttribute("roleIdx", userService.getUserRole(Integer.parseInt(id)).get(0).getId());
        } else {
            user = new User();
        }

        List<Role> roleList = userService.getDomainRolesByAdminRoleIdAndDomainId(lg.getDefRole().getId(), lg
                .getDomainId());
        List<SelectItem> selectItemList = new ArrayList<>();
        if (null != roleList && !roleList.isEmpty()) {
            for (Role r : roleList) {
                selectItemList.add(new SelectItem(r.getId() + "", r.getName()));
            }
        }
        List<SelectItem> genders = new ArrayList<>();
        genders.add(new SelectItem("男", "男"));
        genders.add(new SelectItem("女", "女"));

        model.addAttribute("genders", genders);
        // 动态加载下拉框所有的角色
        model.addAttribute("roles", selectItemList);
        model.addAttribute("domainId", lg.getDomainId());


        return new ModelAndView("view.user.editUser", "command", user);

    }

    @RequestMapping("/selfedit")
    public String selfedit(Model model) {
        Utils.saveLog(lg.getUserId(), "个人信息");
        int roleId = (int) session.getAttribute("roleId");
        if (!validateUser()) {
            return "redirect:/";
        }
        String uid = lg.getUserId() + "";
        model.addAttribute("uid", uid);
        model.addAttribute("roleId", roleId);
        model.addAttribute("command", "");
        return "view.user.selfedit";
    }

    @RequestMapping("/resetpw")
    public String resetpw(Model model) {
        Utils.saveLog(lg.getUserId(), "重置密码");
        int roleId = (int) session.getAttribute("roleId");
        if (!validateUser()) {
            return "redirect:/";
        }
        String uid = lg.getUserId() + "";
        model.addAttribute("uid", uid);
        model.addAttribute("roleId", roleId);
        model.addAttribute("command", "");
        return "view.user.resetPw";
    }

    @RequestMapping(value = "/ajaxSaveUser", method = RequestMethod.POST)
    public String ajaxSaveUser(String name, String nickName, String id, int type, String staffNo, String gender,
                               String mobile, String address, String description, RedirectAttributes attr) {
        Result result;
        if (StringUtils.isEmpty(id) || "0".equals(id)) {
            //新建用户
            result = userService.createUser(name, nickName, type, staffNo, gender, mobile, address, description,
                    lg.getDomainId());
        } else {
            //修改用户
            result = userService.updateUser(nickName, Integer.parseInt(id), type, staffNo, gender, mobile, address,
                    description);
        }
        if (result.getCode().equals(Result.OK)) {
            attr.addFlashAttribute("message", "成功！");
        } else {
            attr.addFlashAttribute("message", "失败！");
        }

        attr.addFlashAttribute("error", true);

        return "redirect:/user";

    }

    @RequestMapping(value = "/ajaxSavePwd", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxSavePwd(String oldpwd, String id, String newpwd) {
        return userService.savePwd(id, oldpwd, newpwd);
    }

    @RequestMapping(value = "/ajaxResetPwd", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxResetPwd(String account, String id, String newpwd) {
        return userService.resetPwd(id, account, newpwd);
    }

    @RequestMapping("/deleteUser")
    public String deleteAdmin(Model model, String id, int mode, RedirectAttributes attr) {
        if (!validateUser()) {
            return "view.login";
        }

        if (Integer.parseInt(id) == lg.getUserId()) {
            attr.addFlashAttribute("error", true);
            attr.addFlashAttribute("message", "不能对自己进行操作");
            return "redirect:/user";
        }

        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Date now = new Date();
            if (!StringUtils.isEmpty(id)) {
                User user = (User) db.get(User.class, Integer.parseInt(id));
                if (user != null) {
                    if (mode == 1) {
                        user.setLastModified(now);
                        attr.addFlashAttribute("message", "人员注销成功");
                        user.setStatus(Constants.UserState.STATE_CANCLE);
                        Utils.saveLog(lg.getUserId(), "注销人员");

                    } else {
                        user.setLastModified(now);
                        attr.addFlashAttribute("message", "人员恢复成功");
                        user.setStatus(Constants.UserState.STATE_NORMAL);
                        Utils.saveLog(lg.getUserId(), "恢复人员");
                    }
                }
            }
            db.commit();

        } finally {
            db.close();
        }
        attr.addFlashAttribute("error", true);
        return "redirect:/user";
    }


    @RequestMapping("/getUsers")
    @ResponseBody
    public Object getUsers(int start, int length) {
        if (!validateUser()) {
            return "";
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, String[]> properties = request.getParameterMap();
        List<User> userList = userService.getUserListByUserId(lg.getUserId());
        if (null == userList || userList.size() < 1) {
            return DBUtils.getEmpty();
        }
        result.put("recordsTotal", userList.size());

        String search = properties.get("search[value]")[0];
        if (search.contains("'")) {
            search = "";
        }
        String status = properties.get("columns[6][search][value]")[0];
        String roleId = properties.get("columns[1][search][value]")[0];
        if (!StringUtils.isEmpty(search)) {
            List<User> users = new ArrayList<>();
            for (User user : userList) {
                if (user.getNickName().contains(search)) {
                    users.add(user);
                }
            }
            userList = users;
        }

        if (!StringUtils.isEmpty(status)) {
            int statusInt = Integer.parseInt(status);
            List<User> users = new ArrayList<>();
            for (User user : userList) {
                if (user.getStatus() == statusInt) {
                    users.add(user);
                }
            }
            userList = users;
        }

        if (!StringUtils.isEmpty(roleId)) {
            int roleIdInt = Integer.parseInt(roleId);
            List<User> users = new ArrayList<>();
            for (User user : userList) {
                if (userService.getUserRole(user.getId()).get(0).getId() == roleIdInt) {
                    users.add(user);
                }
            }
            userList = users;
        }

        if (userList.size() < 1) {
            result.put("recordsFiltered", 0);
            result.put("data", Collections.emptyList());
            return result;
        }

        result.put("recordsFiltered", userList.size());
        if (start * length >= userList.size()) {
            result.put("data", Collections.emptyList());
        } else {
            int endIdx;
            if ((start + 1) * length >= userList.size()) {
                endIdx = userList.size();
            } else {
                endIdx = (start + 1) * length;
            }
            userList = userList.subList(start * length, endIdx);
            result.put("data", userList);
        }

        return result;
    }
}
