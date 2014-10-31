package io.hgc.exwhy.web;

import io.hgc.exwhy.web.authentication.SecurityContext;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;

@Controller
public class GreetingController {
    @RequestMapping("/greeting")
    public String greeting(Model model) {
        model.addAttribute("user", SecurityContext.getCurrentUser());
        return "greeting";
    }
}