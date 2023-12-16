package com.spaulding.WebBuilder.beans;

import com.spaulding.WebBuilder.components.FormLoginCustomizerComponent;
import com.spaulding.WebBuilder.components.OAuth2CustomizerComponent;
import com.spaulding.WebBuilder.objects.CustomAuthenticationProvider;
import com.spaulding.WebBuilder.services.EnvironmentVariableService;
import com.spaulding.WebBuilder.services.UserInformationDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.sql.SQLException;

@Configuration
@EnableWebSecurity
public class SecurityBeans {
    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @Autowired
    private UserInformationDatabaseService userInformationDatabaseService;

    @Autowired
    private OAuth2CustomizerComponent oAuth2CustomizerComponent;

    @Autowired
    private FormLoginCustomizerComponent formLoginCustomizerComponent;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/registrations", "/info", "/verify", "/register-user", "/login", "/login.html", "/webjars/**").permitAll()
                        .requestMatchers("/shutdown-app").hasAuthority("Admin")
                        .anyRequest().authenticated()
                )
                .logout(l -> l
                        .logoutSuccessHandler((req, res, auth) -> req.getSession().setAttribute("message", "Success:You have logged out!"))
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                )
                .csrf(c -> c
                        .ignoringRequestMatchers("/registrations", "/info", "/verify", "/register-user", "/login", "/login.html", "/logout")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .formLogin(formLoginCustomizerComponent)
                .oauth2Login(oAuth2CustomizerComponent)
                .build();
    }

    @Bean
    public CustomAuthenticationProvider getCustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        return new CustomAuthenticationProvider(userDetailsService, encoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public void setupAdminAccount() throws SQLException {
        userInformationDatabaseService.addAccountInfo(environmentVariableService.getAdminEmail(), environmentVariableService.getAdminCredentials(), null, "true");
        userInformationDatabaseService.addGroupToAccount("Admin", environmentVariableService.getAdminEmail());
    }
}
