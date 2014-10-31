package io.hgc.exwhy.web.controllers;

import io.hgc.exwhy.web.authentication.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring4.view.ThymeleafView;

@Controller
public class IndexController {
    @RequestMapping("/")
    public String index(Model model) {
        if (SecurityContext.userSignedIn()) {
            model.addAttribute("user", SecurityContext.getCurrentUser());
        }
        return "index";
    }
}
