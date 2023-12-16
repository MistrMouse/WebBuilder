package com.spaulding.WebBuilder.services;

import com.spaulding.WebBuilder.exceptions.EmailServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class EmailService {
    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DefaultRedirectStrategy defaultRedirectStrategy;

    public String sendVerifyUserEmail(HttpServletRequest req, String toEmail, String key) throws EmailServiceException {
        String verifyURL = getSiteURL(req) + "/verify?username=" + toEmail + "&code=" + key;
        String subject = "Please verify your registration";
        String content = "Dear New User,<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"" + verifyURL + "\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you";

        try {
            sendEmail(toEmail, subject, content);
            return "Success:Verification email has been sent to " + toEmail + "!";
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending verification email!", e);
            throw new EmailServiceException("There was an issue trying to send a verification email at this time!");
        }
    }

    public String sendForgotPasswordEmail(HttpServletRequest req, String toEmail, String key) throws EmailServiceException {
        String resetURL = getSiteURL(req) + "/?username=" + toEmail + "&code=" + key;
        String subject = "Password Reset Request";
        String content = "Dear User,<br>"
                + "Please click the link below to reset your password:<br>"
                + "<h3><a href=\"" + resetURL + "\" target=\"_self\">RESET PASSWORD</a></h3><br>"
                + "If this was not you, the email verification process has been required for future account use!<br>"
                + "You may do so by attempting to log in normally which will send a verification email.<br>"
                + "Thank you";

        try {
            sendEmail(toEmail, subject, content);
            return "Success:Verification email has been sent to " + toEmail;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending verification email!", e);
            throw new EmailServiceException("There was an issue trying to send a verification email at this time!");
        }
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    private void sendEmail(String toEmail, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(environmentVariableService.getSpringMailUsername(), environmentVariableService.getSiteName());
        helper.setTo(toEmail);
        helper.setSubject(subject);

        helper.setText(content, true);

        mailSender.send(message);
    }
}
