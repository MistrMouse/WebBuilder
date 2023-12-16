package com.spaulding.WebBuilder.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class EnvironmentVariableService {
    @Value("${admin-email}")
    private String adminEmail;

    @Value("${admin-credentials}")
    private String adminCredentials;

    @Value("${datasource-classname}")
    private String datasourceClassName;

    @Value("${datasource-url}")
    private String datasourceUrl;

    @Value("${datasource-username}")
    private String datasourceUserName;

    @Value("${datasource-credentials}")
    private String datasourceCredentials;

    @Value("${registration-name}")
    private String registrationName;

    @Value("${registration-client-id}")
    private String registrationClientId;

    @Value("${registration-client-secret}")
    private String registrationClientSecret;

    @Value("${registration-scopes:#{null}}")
    private String registrationScopes;

    @Value("${registration-authorization-uri:#{null}}")
    private String registrationAuthorizationUri;

    @Value("${registration-token-uri:#{null}}")
    private String registrationTokenUri;

    @Value("${registration-user-info-uri:#{null}}")
    private String registrationUserInfoUri;

    @Value("${registration-jwk-set-uri:#{null}}")
    private String registrationJwkSetUri;

    @Value("${registration-user-name-attr:#{null}}")
    private String registrationUserNameAttr;

    @Value("${registration-email-callback:#{null}}")
    private String registrationEmailCallback;

    @Value("${registration-email-field-name}")
    private String registrationEmailFieldName;

    @Value("${registration-email-verified-callback:#{null}}")
    private String registrationEmailVerifiedCallback;

    @Value("${registration-email-verified-field-name}")
    private String registrationEmailVerifiedFieldName;

    @Value("${spring.mail.username}")
    private String springMailUsername;

    @Value("${site-name}")
    private String siteName;
}
