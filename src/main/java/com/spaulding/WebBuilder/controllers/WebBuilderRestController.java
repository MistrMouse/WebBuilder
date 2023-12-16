package com.spaulding.WebBuilder.controllers;

import com.spaulding.WebBuilder.beans.RegistrationBeans;
import com.spaulding.WebBuilder.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
@RestController
public class WebBuilderRestController implements ApplicationContextAware {
    @Autowired
    private UserService userService;

    private ApplicationContext ctx;

    @GetMapping("/info")
    public Object getInfo(HttpServletRequest request, @RequestParam("info") String info, @RequestParam(name = "deleteAfterRetrieve", required = false, defaultValue = "false") boolean deleteAfterRetrieve) {
        Object attr = request.getSession().getAttribute(info);
        if (deleteAfterRetrieve) {
            request.getSession().removeAttribute(info);
        }
        return attr;
    }

    @GetMapping("/registrations")
    public Map<String, Object> getRegistrations() {
        return Collections.singletonMap("registrations", RegistrationBeans.registrationNames);
    }

    @GetMapping("/shutdown-app")
    public void shutdown() {
        ((ConfigurableApplicationContext) ctx).close();
    }

    @GetMapping("/verify")
    public void verifyUser(HttpServletRequest req, HttpServletResponse res, @Param("username") String username, @Param("code") String code) throws IOException {
        userService.verifyNewUser(req, res, username, code);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(HttpServletRequest req, @RequestParam("username") String username) {
        return userService.resetRegistration(req, username);
    }

    @PostMapping("/reset-password")
    public void resetPassword(HttpServletRequest req, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("code") String code) {

    }

    @PostMapping("/register-user")
    public String registerUser(HttpServletRequest req, @RequestParam("username") String username, @RequestParam("password") String password) {
        return userService.registerNewUser(req, username, password);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }
}
