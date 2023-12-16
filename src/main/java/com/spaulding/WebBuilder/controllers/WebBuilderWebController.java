package com.spaulding.WebBuilder.controllers;

import com.spaulding.WebBuilder.services.WebBuilderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebBuilderWebController {
    @Autowired
    private WebBuilderService webBuilderService;

    @GetMapping("/")
    public String get(Authentication authentication, @RequestParam("request-name") String requestName) {
        return webBuilderService.getHtml(authentication, requestName);
    }

    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest req) {
        req.getSession().removeAttribute("email");
        req.getSession().removeAttribute("groups");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login.html");
        return modelAndView;
    }
}
