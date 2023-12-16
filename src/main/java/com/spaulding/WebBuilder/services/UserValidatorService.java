package com.spaulding.WebBuilder.services;

import com.spaulding.WebBuilder.services.UserInformationDatabaseService;
import com.spaulding.tools.Archive.Archive;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserValidatorService {
    @Autowired
    private UserInformationDatabaseService userInformationService;

    public void validate(String email, HttpServletRequest req) throws SQLException {
        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = req.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = req.getRemoteAddr();
        }
        ipAddress = ipAddress.split(",")[0].trim();

        String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ipAddress);
        if (!matcher.matches()) {
            throw new RuntimeException("Unable to authenticate Client IP Address!");
        }

        if (userInformationService.isBannedIPAddress(ipAddress)) {
            throw new RuntimeException("The given ip address has been banned, please be sure you are not on a VPN!");
        }

        List<Archive.Row> bans = userInformationService.getBanInformation(email);
        if (!bans.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean allow = true;
            sb.append("The following is a list of past infractions:");
            for (int i = 0; i < bans.size(); i++) {
                Archive.Row ban = bans.get(i);
                sb.append("\n");
                sb.append(i + 1);
                sb.append(". ");
                sb.append(ban.getResult(1));
                if ((Boolean) ban.getResult(2)) {
                    sb.append(" [Status: Lifted]");
                } else {
                    sb.append(" [Status: Current]");
                    allow = false;
                }
            }

            if (!allow) {
                throw new RuntimeException("You no longer are allowed access to the given resources.\n" + sb);
            }
        }

        List<Archive.Row> accountInfo = userInformationService.getAccountInfo(email);
        if (accountInfo.isEmpty()) {
            userInformationService.addAccountInfo(email, null, ipAddress, "true");
        }
        else {
            Archive.Row account = accountInfo.get(0);
            String ipAddresses = (String) account.getResult(2);
            boolean validated = account.getResult(4).equals("true");
            if (!ipAddresses.contains(ipAddress)) {
                ipAddresses += "," + ipAddress;
                userInformationService.updateAccountInfo(email, (String) account.getResult(1), ipAddresses);
            }

            if (!validated) {
                throw new RuntimeException("The given account has not been validated yet, please check your email!");
            }
        }
    }
}
