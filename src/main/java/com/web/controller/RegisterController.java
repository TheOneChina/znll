package com.web.controller;

import com.tnsoft.web.model.Result;
import com.tnsoft.web.model.SelectItem;
import com.tnsoft.web.service.RegisterService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/register")
public class RegisterController extends BaseController {

    @Resource(name = "registerService")
    private RegisterService registerService;

    public RegisterController() {
        super();
    }

    @RequestMapping("/page")
    public String register(Model model, HttpServletRequest request) {
        session.removeAttribute("ERROR");
        List<SelectItem> versions = new ArrayList<SelectItem>();
        versions.add(new SelectItem("6", "标准"));
        versions.add(new SelectItem("2", "医药"));
        model.addAttribute("versions", versions);
        model.addAttribute("version	", "2");
        request.setAttribute("sendFlag", "0");

        return "view.register";
    }

    @RequestMapping("/sendCode")
    @ResponseBody
    public Object sendCode(String mobile, HttpSession session) {
        Result result = registerService.sendCode(mobile);
        if (Result.OK.equals(result.getCode())) {
            session.setAttribute("smscode", result.getMessage());
            result.setMessage(null);
        }
        return result;
    }

    @RequestMapping(value = "/isValid", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Result isValid(String username) {
        Result result = new Result();
        if (username.length() < 8) {
            result.setCode(Result.ERROR);
            result.setMessage("字符长度不得低于8位!");
        } else if (!Utils.isValidUsername(username)) {
            result.setCode(Result.ERROR);
            result.setMessage("用户名只能包含数字和字母!");
        } else if (registerService.isUsernameAble(username)) {
            result.setCode(Result.OK);
        } else {
            result.setCode(Result.ERROR);
            result.setMessage("用户名已存在!");
        }
        return result;
    }

    @RequestMapping(value = "/mobileValid", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Result mobileValidation(String mobile) {
        Result result = new Result();
        if (!Utils.isMobileNO(mobile)) {
            result.setCode(Result.ERROR);
            result.setMessage("请输入正确的手机号");
            return result;
        } else {
            result.setCode(Result.OK);
        }
        /*if(registerService.isMobileAble(mobile)){
            result.setCode(Result.OK);
		}else{
			result.setCode(Result.ERROR);
			result.setMessage("该手机号已经存在，不能使用!");
		}*/
        return result;
    }


    @RequestMapping("/sendMobileCode")
    @ResponseBody
    public Object sendMobileCode(String mobile) {
        return registerService.sendCode(mobile);
    }

    @RequestMapping(value = "/mobileSubmit", method = RequestMethod.POST)
    @ResponseBody
    public Result submitMobile(String username, String password, String phone, String version) {
        return registerService.creatNewUserAndDomain(username.trim(), password.trim(), phone.trim(), version);
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public String submitRegister(Model model, String username, String password, String phone, String version, String
            smscode) {
        Result test = new Result();
        if (!smscode.equals(session.getAttribute("smscode"))) {
            test.setCode(Result.ERROR);
            if (session.getAttribute("smscode") == null) {
                test.setMessage("验证码不存在或已经失效！");
            } else {
                test.setMessage("验证码不一致！");
            }
        } else {
            test = registerService.creatNewUserAndDomain(username.trim(), password.trim(), phone.trim(), version);
            session.setAttribute("roleId", version);
        }
        model.addAttribute("result", test.getMessage());
        if (Result.OK.equals(test.getCode())) {
            //request.removeAttribute("ERROR");
            request.setAttribute("username", username);
            return "view.ok";
        } else {
            request.setAttribute("ERROR", test.getMessage());
            request.setAttribute("username", username);
            request.setAttribute("sendFlag", "1");
            request.setAttribute("password", password);
            request.setAttribute("phone", phone);
            request.setAttribute("version", version);
            return "view.register";
        }
    }
}
