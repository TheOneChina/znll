package com.web.controller;

import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.model.Response;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class ProfileController extends BaseController {

    public ProfileController() {
        super();
    }

    @RequestMapping("/profile")
    public String profile(Model model) {


        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("nickname", lg.getNickName());
        model.addAttribute("id", lg.getUserId());

        return "view.profile.profile";
    }

    @RequestMapping("/ajaxChangeProfile")
    @ResponseBody
    public Object ajaxChangeProfile(int id, String name, String pwd) {

        if (!validateUser()) {
            Response resp = new Response(Response.ERROR);
            resp.setMessage("操作失败！");
            return resp;
        }

        Response resp = new Response(Response.OK);

        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Date now = new Date();
            User user = (User) db.get(User.class, id);
            if (user != null) {
                user.setName(name);
                user.setPassword(pwd);
                user.setLastModified(now);

                resp.setMessage("修改成功！");
            } else {
                resp.setCode(Response.ERROR);
                resp.setMessage("修改失败，该用户不存在！");
            }


            db.commit();

        } finally {
            db.close();
        }

        return resp;
    }


}
