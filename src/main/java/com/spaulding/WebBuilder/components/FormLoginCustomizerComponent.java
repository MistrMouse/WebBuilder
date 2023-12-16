package com.spaulding.WebBuilder.components;

import com.spaulding.WebBuilder.services.UserInformationDatabaseService;
import com.spaulding.WebBuilder.services.UserValidatorService;
import com.spaulding.tools.Archive.Archive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FormLoginCustomizerComponent implements Customizer<FormLoginConfigurer<HttpSecurity>> {
    @Autowired
    private UserInformationDatabaseService userInformationDatabaseService;

    @Autowired
    private UserValidatorService userValidatorService;

    @Autowired
    private DefaultRedirectStrategy defaultRedirectStrategy;

    @Override
    public void customize(FormLoginConfigurer<HttpSecurity> f) {
        f
            .loginPage("/login")
            .successHandler((req, res, auth) -> {
                try {
                    try {
                        String email = auth.getName();
                        List<Archive.Row> groupInfo = userInformationDatabaseService.getGroups(email);
                        List<String> groups = new ArrayList<>();
                        groups.add("User");
                        for (Archive.Row row : groupInfo) {
                            groups.add((String) row.getResult(1));
                        }

                        if (!groups.contains("Admin")) {
                            userValidatorService.validate(email, req);
                        }

                        req.getSession().setAttribute("groups", groups);
                        req.getSession().setAttribute("email", email);
                        req.getSession().setAttribute("message", "Success:You are now logged in!");
                        defaultRedirectStrategy.sendRedirect(req, res, "/?request-name=home");
                    } catch (SQLException e) {
                        log.info(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
                catch (RuntimeException ex) {
                    req.getSession().setAttribute("message", ex.getMessage());
                    auth.setAuthenticated(false);
                    defaultRedirectStrategy.sendRedirect(req, res, "/login");
                }
            })
            .failureHandler((req, res, ex) -> {
                if (ex instanceof UsernameNotFoundException) {
                    req.getSession().setAttribute("validation-email-feedback", "The email entered was not found under user accounts!");
                }
                else if (ex instanceof BadCredentialsException) {
                    req.getSession().setAttribute("validation-password-feedback", "The password entered was incorrect! Attempts remaining: ");
                }
                else {
                    req.getSession().setAttribute("message", ex.getMessage());
                }
                defaultRedirectStrategy.sendRedirect(req, res, "/login");
            });
    }
}
