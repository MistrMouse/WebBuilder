package com.spaulding.WebBuilder.beans;

import com.spaulding.WebBuilder.services.EnvironmentVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseBeans {
    @Autowired
    private EnvironmentVariableService environmentVariableService;

    @Bean
    public JdbcTemplate getJdbcTemplate() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(environmentVariableService.getDatasourceClassName());
        driverManagerDataSource.setUrl(environmentVariableService.getDatasourceUrl());
        driverManagerDataSource.setUsername(environmentVariableService.getDatasourceUserName());
        driverManagerDataSource.setPassword(environmentVariableService.getDatasourceCredentials());
        return new JdbcTemplate(driverManagerDataSource);
    }
}
