package com.spaulding.WebBuilder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class WebBuilderService {
    @Autowired
    private WebBuilderDatabaseService webBuilderDatabaseService;

    public String getHtml(Authentication authentication, String requestName) {
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }

        try {
            return webBuilderDatabaseService.buildElement(roles, requestName);
        } catch (SQLException e) {
            return "Unable to access the given site at this time, please try again later!";
        }
    }
}
