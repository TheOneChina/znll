package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelpController extends BaseController {
    @RequestMapping("/introduce/{flag}")
    public String introduce(Model model, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "help.express.introduce";
                break;
            case "2":
                view = "help.medicine.introduce";
                break;
            case "3":
                view = "help.standard.introduce";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/operation/{flag}")
    public String operation(Model model, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "help.express.operation";
                break;
            case "2":
                view = "help.medicine.operation";
                break;
            case "3":
                view = "help.standard.operation";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/question/{flag}")
    public String question(Model model, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "help.express.question";
                break;
            case "2":
                view = "help.medicine.question";
                break;
            case "3":
                view = "help.standard.question";
                break;
            default:
        }
        return view;
    }

    @RequestMapping("/aboutus/{flag}")
    public String aboutus(Model model, @PathVariable String flag) {
        if (!validateUser()) {
            return "redirect:/";
        }
        String view = "";
        switch (flag) {
            case "1":
                view = "help.express.aboutus";
                break;
            case "2":
                view = "help.medicine.aboutus";
                break;
            case "3":
                view = "help.standard.aboutus";
                break;
            default:
        }
        return view;
    }
}
