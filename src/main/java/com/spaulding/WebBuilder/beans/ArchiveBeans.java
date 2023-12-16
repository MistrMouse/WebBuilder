package com.spaulding.WebBuilder.beans;

import com.spaulding.WebBuilder.services.SecurityDatabaseService;
import com.spaulding.WebBuilder.services.UserInformationDatabaseService;
import com.spaulding.WebBuilder.services.WebBuilderDatabaseService;
import com.spaulding.tools.Archive.services.DBEncryptionKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

@Configuration
public class ArchiveBeans {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public DBEncryptionKeyService getDBEncryptionKeyService() throws SQLException {
        return new DBEncryptionKeyService(jdbcTemplate);
    }
}
