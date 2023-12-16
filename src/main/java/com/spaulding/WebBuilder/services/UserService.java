package com.spaulding.WebBuilder.services;

import com.spaulding.WebBuilder.exceptions.EmailServiceException;
import com.spaulding.tools.Archive.Archive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Service;
import org.springframework.data.repository.query.Param;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserInformationDatabaseService userInformationDatabaseService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DefaultRedirectStrategy defaultRedirectStrategy;

    public String registerNewUser(HttpServletRequest req, String userName, String password) {
        try {
            List<Archive.Row> rows = userInformationDatabaseService.getAccountInfo(userName);
            if (!rows.isEmpty()) {
                String key = (String) rows.get(0).getResult(4);
                if (!key.equals("true")) {
                    return emailService.sendVerifyUserEmail(req, userName, key);
                } else {
                    return "An account is already registered with the provided email!";
                }
            }

            String key = RandomString.make(64);
            userInformationDatabaseService.addAccountInfo(userName, password, "", key);
            return emailService.sendVerifyUserEmail(req, userName, key);
        }
        catch (SQLException e) {
            log.error("Error accessing database!", e);
            return "There was an issue trying to interact with the server database!";
        } catch (EmailServiceException e) {
            return e.getMessage();
        }
    }

    public String resetRegistration(HttpServletRequest req, String userName) {
        try {
            List<Archive.Row> rows = userInformationDatabaseService.getAccountInfo(userName);
            if (rows.isEmpty()) {
                return "Unable to find the associated account with the given email!";
            }

            Archive.Row row = rows.get(0);
            String key = (String) row.getResult(4);
            key = key.equals("true") ? RandomString.make(64) : key;
            userInformationDatabaseService.updateAccountInfo(userName, (String) row.getResult(1), (String) row.getResult(2), key);
            return emailService.sendForgotPasswordEmail(req, userName, key);
        }
        catch (SQLException e) {
            log.error("Error accessing database!", e);
            return "There was an issue trying to interact with the server database!";
        } catch (EmailServiceException e) {
            return e.getMessage();
        }
    }

    public void verifyNewUser(HttpServletRequest req, HttpServletResponse res, @Param("username") String username, @Param("code") String code) throws IOException {
        try {
            List<Archive.Row> rows = userInformationDatabaseService.getAccountInfo(username);
            if (rows.isEmpty()) {
                defaultRedirectStrategy.sendRedirect(req, res, "/");
                return;
            }
            Archive.Row row = rows.get(0);
            if (!row.getResult(4).equals(code)) {
                req.getSession().setAttribute("message", "Invalid verification code given, please be sure to follow the link through your email!");
                defaultRedirectStrategy.sendRedirect(req, res, "/");
                return;
            }
            userInformationDatabaseService.updateAccountInfo((String) row.getResult(0), (String) row.getResult(1), (String) row.getResult(2));
            req.getSession().setAttribute("message", "Success:You are now verified, you may now log in!");
        }
        catch (SQLException e) {
            log.info(e.getMessage());
            req.getSession().setAttribute("message", "There was an issue trying to interact with the server database!");
        }
        defaultRedirectStrategy.sendRedirect(req, res, "/");
    }
}
