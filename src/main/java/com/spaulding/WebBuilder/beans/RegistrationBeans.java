package com.spaulding.WebBuilder.beans;

import com.spaulding.WebBuilder.services.EnvironmentVariableService;
import com.spaulding.WebBuilder.services.SecurityDatabaseService;
import com.spaulding.tools.Archive.Archive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RegistrationBeans {
    public static List<String> registrationNames = new ArrayList<>();

    @Autowired
    private SecurityDatabaseService securityDatabaseService;

    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @Bean
    public OAuth2AuthorizedClientService getOAuth2AuthorizedClientService() throws Exception {
        return new InMemoryOAuth2AuthorizedClientService(getClientRegistrationRepository());
    }

    @Bean
    public ClientRegistrationRepository getClientRegistrationRepository() throws Exception {
        securityDatabaseService.addRegistration(
                environmentVariableService.getRegistrationName(),
                environmentVariableService.getRegistrationClientId(),
                environmentVariableService.getRegistrationClientSecret(),
                environmentVariableService.getRegistrationScopes(),
                environmentVariableService.getRegistrationAuthorizationUri(),
                environmentVariableService.getRegistrationTokenUri(),
                environmentVariableService.getRegistrationUserInfoUri(),
                environmentVariableService.getRegistrationJwkSetUri(),
                environmentVariableService.getRegistrationUserNameAttr(),
                environmentVariableService.getRegistrationEmailCallback(),
                environmentVariableService.getRegistrationEmailFieldName(),
                environmentVariableService.getRegistrationEmailVerifiedCallback(),
                environmentVariableService.getRegistrationEmailVerifiedFieldName()
        );

        List<Archive.Row> registrations = securityDatabaseService.getRegistrations();
        ClientRegistration[] clientRegistrations = new ClientRegistration[registrations.size()];
        for (int i = 0; i < registrations.size(); i++) {
            Archive.Row registration = registrations.get(i);
            String name = (String) registration.getResult(0);
            registrationNames.add(name);

            ClientRegistration.Builder builder = switch (name) {
                case "google" -> CommonOAuth2Provider.GOOGLE.getBuilder(name);
                case "github" -> CommonOAuth2Provider.GITHUB.getBuilder(name);
                case "okta" -> CommonOAuth2Provider.OKTA.getBuilder(name);
                case "facebook" -> CommonOAuth2Provider.FACEBOOK.getBuilder(name);
                default -> ClientRegistration.withRegistrationId(name)
                        .clientName(name.substring(0, 1).toUpperCase() + name.substring(1))
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}");
            };

            builder
                    .clientId((String) registration.getResult(1))
                    .clientSecret((String) registration.getResult(2));

            if (registration.getResult(1) != null && !registration.getResult(1).equals("")) {
                builder.clientId((String) registration.getResult(1));
            }
            if (registration.getResult(2) != null && !registration.getResult(2).equals("")) {
                builder.clientSecret((String) registration.getResult(2));
            }
            if (registration.getResult(3) != null && !registration.getResult(3).equals("")) {
                builder.scope(((String) registration.getResult(3)).split(","));
            }
            if (registration.getResult(4) != null && !registration.getResult(4).equals("")) {
                builder.authorizationUri((String) registration.getResult(4));
            }
            if (registration.getResult(5) != null && !registration.getResult(5).equals("")) {
                builder.tokenUri((String) registration.getResult(5));
            }
            if (registration.getResult(6) != null && !registration.getResult(6).equals("")) {
                builder.userInfoUri((String) registration.getResult(6));
            }
            if (registration.getResult(7) != null && !registration.getResult(7).equals("")) {
                builder.jwkSetUri((String) registration.getResult(7));
            }
            if (registration.getResult(8) != null && !registration.getResult(8).equals("")) {
                builder.userNameAttributeName((String) registration.getResult(8));
            }

            clientRegistrations[i] = builder.build();
        }

        return new InMemoryClientRegistrationRepository(clientRegistrations);
    }
}
