package com.spaulding.WebBuilder.services;

import com.spaulding.WebBuilder.objects.CustomAuthority;
import com.spaulding.tools.Archive.Archive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class UserSecurityDetailService implements UserDetailsService {
    @Autowired
    protected UserInformationDatabaseService userInformationDatabaseService;

    @Autowired
    protected PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        List<Archive.Row> account;
        List<Archive.Row> groupInfo;
        try {
            account = userInformationDatabaseService.getAccountInfo(usernameOrEmail);
            if (account.isEmpty()) {
                throw new UsernameNotFoundException("Username not found");
            }

            groupInfo = userInformationDatabaseService.getGroups(usernameOrEmail);
        } catch (SQLException e) {
            log.info(e.getMessage());
            throw new RuntimeException("There was an issue trying to access the database for verification!");
        }

        List<String> groups = new ArrayList<>();
        groups.add("User");
        for (Archive.Row row : groupInfo) {
            groups.add((String) row.getResult(1));
        }

        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String group : groups) {
            authorities.add(new CustomAuthority(group));
        }

        return new User(
                usernameOrEmail,
                encoder.encode((String) account.get(0).getResult(1)),
                authorities
        );
    }
}
