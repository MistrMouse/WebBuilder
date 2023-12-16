package com.spaulding.WebBuilder.services;

import com.spaulding.tools.Archive.Archive;
import com.spaulding.tools.Archive.services.DBEncryptionKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class SecurityDatabaseService extends Archive {
    @Autowired
    private DBEncryptionKeyService dbEncryptionKeyService;

    private String key;

    public SecurityDatabaseService(JdbcTemplate jdbcTemplate) throws SQLException {
        super("SecurityDatabaseService", jdbcTemplate);
    }

    @Override
    protected void setup() throws SQLException {
        setupRegistrations();
    }

    private void setupRegistrations() throws SQLException {
        jdbc.execute("CREATE TABLE IF NOT EXISTS registrations (name VARCHAR, client_id VARCHAR, client_secret VARCHAR, scopes VARCHAR, authorization_uri VARCHAR, token_uri VARCHAR, user_info_uri VARCHAR, jwk_set_uri VARCHAR, user_name_attr VARCHAR, email_callback VARCHAR, email_field_name VARCHAR NOT NULL, email_verified_callback VARCHAR, email_verified_field_name VARCHAR, CHECK(TRIM(email_field_name) != ''), UNIQUE(name))");

        Object[] args = new Object[] {
                "Registrations" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO registrations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                13,
                "String,String,String,String,String,String,String,String,String,String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Registrations" + SELECT_FOLLOWER,
                "SELECT * FROM registrations",
                0,
                null
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Registration" + SELECT_FOLLOWER,
                "SELECT * FROM registrations WHERE name = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Registrations" + UPDATE_FOLLOWER,
                "UPDATE registrations SET client_id = ?, client_secret = ?, scopes = ?, authorization_uri = ?, token_uri = ?, user_info_uri = ?, jwk_set_uri = ?, user_name_attr = ?, email_callback = ?, email_field_name = ?, email_verified_callback = ?, email_verified_field_name = ? WHERE name = ?",
                13,
                "String,String,String,String,String,String,String,String,String,String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);
    }

    private String getKey() throws SQLException {
        if (key == null) {
            dbEncryptionKeyService.registerNewKey("SecurityDatabaseServiceKey");
            key = dbEncryptionKeyService.getKey("SecurityDatabaseServiceKey");

            if (key == null) {
                throw new SQLException("Unable to access database encryption key for the SecurityDatabaseService class.");
            }
        }

        return key;
    }

    public List<Row> getRegistrations() throws SQLException {
        return execute(SYSADMIN, "Registrations" + SELECT_FOLLOWER, getKey());
    }

    public List<Row> getRegistration(String name) throws SQLException {
        return execute(SYSADMIN, "Registration" + SELECT_FOLLOWER, new Object[]{name}, getKey());
    }

    public void addRegistration(String name, String clientId, String clientSecret, String scopes, String authorizationUri, String tokenUri, String userInfoUri, String jwkSetUri, String userNameAttr, String emailCallback, String emailFieldName, String emailVerifiedCallback, String emailVerifiedFieldName) throws SQLException {
        execute(SYSADMIN, "Registrations" + INSERT_FOLLOWER, new Object[]{
                name,
                clientId,
                clientSecret,
                scopes,
                authorizationUri,
                tokenUri,
                userInfoUri,
                jwkSetUri,
                userNameAttr,
                emailCallback,
                emailFieldName,
                emailVerifiedCallback,
                emailVerifiedFieldName
        }, getKey());
    }
}
