package com.spaulding.WebBuilder.components;

import com.spaulding.WebBuilder.objects.CustomAuthority;
import com.spaulding.WebBuilder.services.UserInformationDatabaseService;
import com.spaulding.WebBuilder.services.UserValidatorService;
import org.json.JSONObject;
import com.spaulding.WebBuilder.services.RestCallService;
import com.spaulding.WebBuilder.services.SecurityDatabaseService;
import com.spaulding.tools.Archive.Archive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
public class OAuth2CustomizerComponent implements Customizer<OAuth2LoginConfigurer<HttpSecurity>> {

    @Autowired
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @Autowired
    private UserInformationDatabaseService userInformationDatabaseService;

    @Autowired
    private SecurityDatabaseService securityDatabaseService;

    @Autowired
    private RestCallService restCallService;

    @Autowired
    private UserValidatorService userValidatorService;

    @Autowired
    private DefaultRedirectStrategy defaultRedirectStrategy;

    @Override
    public void customize(OAuth2LoginConfigurer<HttpSecurity> o) {
        o
                .loginPage("/login")
                .successHandler((req, res, auth) -> {
                    OAuth2AuthenticationToken info = (OAuth2AuthenticationToken) auth;
                    OAuth2AuthorizedClient client;

                    try {
                        try {
                            client = oAuth2AuthorizedClientService.loadAuthorizedClient(info.getAuthorizedClientRegistrationId(), info.getName());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            String email;
                            boolean verified = false;

                            List<Archive.Row> response = securityDatabaseService.getRegistration(client.getClientRegistration().getClientName().toLowerCase());
                            if (response.isEmpty()) {
                                throw new RuntimeException("Unable to find given login method!");
                            }

                            Archive.Row registration = response.get(0);
                            String emailCallback = (String) registration.getResult(9);
                            String emailFieldName = (String) registration.getResult(10);
                            String emailVerifiedCallback = (String) registration.getResult(11);
                            String emailVerifiedFieldName = (String) registration.getResult(12);
                            if (emailFieldName == null) {
                                throw new RuntimeException("Unable to sign in with given method due to server side setup issues: unspecified email field name");
                            }

                            boolean isDefaultEmailWithFieldName = (emailVerifiedCallback == null || emailVerifiedCallback.isEmpty()) && !emailVerifiedFieldName.isEmpty();
                            if (emailCallback == null || emailCallback.isEmpty()) {
                                email = ((OAuth2User) auth.getPrincipal()).getAttribute(emailFieldName);
                                if (isDefaultEmailWithFieldName) {
                                    verified = Boolean.TRUE.equals(((OAuth2User) auth.getPrincipal()).getAttribute(emailVerifiedFieldName));
                                }
                            } else {
                                HttpHeaders headers = new HttpHeaders();
                                headers.set("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
                                JSONObject json = restCallService.get(emailCallback, headers, null).getBody();
                                if (json == null || json.isEmpty()) {
                                    throw new RuntimeException("Unable to verify given email information with the provider at this time!");
                                }

                                email = json.get("0") == null ? (String) json.get(emailFieldName) : (String) ((JSONObject) json.get("0")).get(emailFieldName);
                                if (isDefaultEmailWithFieldName) {
                                    verified = (json.get("0") == null ? (Boolean) json.get(emailVerifiedFieldName) : (Boolean) ((JSONObject) json.get("0")).get(emailVerifiedFieldName)) || Boolean.TRUE.equals(((OAuth2User) auth.getPrincipal()).getAttribute(emailVerifiedFieldName));
                                }
                            }

                            if (emailVerifiedCallback != null && !emailVerifiedCallback.isEmpty() && !emailVerifiedFieldName.isEmpty()) {
                                HttpHeaders headers = new HttpHeaders();
                                headers.set("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
                                JSONObject json = restCallService.get(emailVerifiedCallback, headers, null).getBody();
                                if (json == null || json.isEmpty()) {
                                    throw new RuntimeException("Unable to verify given email verification information with the provider at this time!");
                                }

                                verified = json.get("0") == null ? (Boolean) json.get(emailVerifiedFieldName) : (Boolean) ((JSONObject) json.get("0")).get(emailVerifiedFieldName);
                            }

                            if (!emailVerifiedFieldName.isEmpty() && !verified) {
                                throw new RuntimeException("User is not using a verified email with the given sign in provider!");
                            }

                            if (email == null || email.isEmpty()) {
                                throw new RuntimeException("Unable to retrieve user email from the given sign in provider!");
                            }

                            List<Archive.Row> groupInfo = userInformationDatabaseService.getGroups(email);
                            List<String> groups = new ArrayList<>();
                            groups.add("User");
                            for (Archive.Row row : groupInfo) {
                                groups.add((String) row.getResult(1));
                            }

                            if (!groups.contains("Admin")) {
                                userValidatorService.validate(email, req);
                            }

                            Collection<GrantedAuthority> authorities = new HashSet<>(info.getAuthorities());
                            for (String group : groups) {
                                authorities.add(new CustomAuthority(group));
                            }
                            OAuth2AuthenticationToken newOauthToken = new OAuth2AuthenticationToken(
                                    info.getPrincipal(),
                                    authorities,
                                    info.getAuthorizedClientRegistrationId()
                            );
                            SecurityContextHolder.getContext().setAuthentication(newOauthToken);

                            req.getSession().setAttribute("groups", groups);
                            req.getSession().setAttribute("email", email);
                            req.getSession().setAttribute("message", "Success:You are now logged in!");
                            defaultRedirectStrategy.sendRedirect(req, res, "/?request-name=home");
                        } catch (SQLException e) {
                            log.info(e.getMessage());
                            throw new RuntimeException("Unable to query server database at this time for log in verification, please try again later!");
                        }
                    }
                    catch (RuntimeException ex) {
                        req.getSession().setAttribute("message", ex.getMessage());
                        auth.setAuthenticated(false);
                        defaultRedirectStrategy.sendRedirect(req, res, "/login");
                    }
                })
                .failureHandler(((req, res, ex) -> {
                    req.getSession().setAttribute("message", ex.getMessage());
                    defaultRedirectStrategy.sendRedirect(req, res, "/login");
                }));
    }
}
