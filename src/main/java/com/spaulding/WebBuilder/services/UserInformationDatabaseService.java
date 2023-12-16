package com.spaulding.WebBuilder.services;

import com.spaulding.tools.Archive.Archive;
import com.spaulding.tools.Archive.services.DBEncryptionKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserInformationDatabaseService extends Archive {
    @Autowired
    private DBEncryptionKeyService dbEncryptionKeyService;

    private String key;

    public UserInformationDatabaseService(JdbcTemplate jdbcTemplate) throws SQLException {
        super("UserInformationDatabaseService", jdbcTemplate);
    }

    @Override
    protected void setup() throws SQLException {
        setupAccounts();
        setupBans();
        setupGroups();
    }

    private void setupBans() throws SQLException {
        jdbc.execute("CREATE TABLE IF NOT EXISTS ip_bans (ip VARCHAR, UNIQUE(ip))");
        jdbc.execute("CREATE TABLE IF NOT EXISTS email_bans (email VARCHAR, reason VARCHAR, lifted VARCHAR, UNIQUE(email))");

        Object[] args = new Object[] {
                "IPBans" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO ip_bans VALUES (?)",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "EmailBans" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO email_bans VALUES (?, ?, ?)",
                3,
                "String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "IPBans" + SELECT_FOLLOWER,
                "SELECT * FROM ip_bans WHERE ip = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "EmailBans" + SELECT_FOLLOWER,
                "SELECT * FROM email_bans WHERE email = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);
    }

    private void setupAccounts() throws SQLException {
        jdbc.execute("CREATE TABLE IF NOT EXISTS accounts (email VARCHAR, credentials VARCHAR, ip_addresses VARCHAR, join_date VARCHAR, verified VARCHAR, UNIQUE(email))");

        Object[] args = new Object[] {
                "Accounts" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO accounts VALUES (?, ?, ?, ?, ?)",
                5,
                "String,String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Accounts" + SELECT_FOLLOWER,
                "SELECT * FROM ACCOUNTS WHERE email = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Accounts" + UPDATE_FOLLOWER,
                "UPDATE accounts SET credentials = ?, ip_addresses = ?, verified = ? WHERE email = ?",
                4,
                "String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);
    }

    private void setupGroups() throws SQLException {
        jdbc.execute("CREATE TABLE IF NOT EXISTS permission_groups (permission_group VARCHAR, UNIQUE(permission_group))");
        jdbc.execute("CREATE TABLE IF NOT EXISTS account_group_xref (email VARCHAR, permission_group VARCHAR, FOREIGN KEY(email) REFERENCES accounts(email), FOREIGN KEY(permission_group) REFERENCES permission_groups(permission_group), UNIQUE(email, permission_group))");

        Object[] args = new Object[] {
                "Groups" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO permission_groups VALUES (?)",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Account-Group-Xref" + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO account_group_xref VALUES (?, ?)",
                2,
                "String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[] {
                "Account-Group-Xref" + SELECT_FOLLOWER,
                "SELECT * FROM account_group_xref WHERE email = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        execute(SYSADMIN, "Groups" + INSERT_FOLLOWER, new Object[]{"Admin"});
    }

    private String getKey() throws SQLException {
        if (key == null) {
            dbEncryptionKeyService.registerNewKey("UserInformationDatabaseServiceKey");
            key = dbEncryptionKeyService.getKey("UserInformationDatabaseServiceKey");

            if (key == null) {
                throw new SQLException("Unable to access database encryption key for the UserInformationDatabaseService class.");
            }
        }

        return key;
    }

    public Boolean isBannedIPAddress(String ipAddress) throws SQLException {
        return !execute(SYSADMIN, "IPBans" + SELECT_FOLLOWER, new Object[]{ipAddress}, getKey()).isEmpty();
    }

    public List<Row> getBanInformation(String email) throws SQLException {
        return execute(SYSADMIN, "IDBans" + SELECT_FOLLOWER, new Object[]{email}, getKey());
    }

    public List<Row> getAccountInfo(String email) throws SQLException {
        return execute(SYSADMIN, "Accounts" + SELECT_FOLLOWER, new Object[]{email}, getKey());
    }

    public void addAccountInfo(String email, String credentials, String ipAddress, String validated) throws SQLException {
        execute(SYSADMIN, "Accounts" + INSERT_FOLLOWER, new Object[]{email, credentials, ipAddress, LocalDateTime.now().toString(), validated}, getKey());
    }

    public void updateAccountInfo(String email, String credentials, String ipAddresses) throws SQLException {
        updateAccountInfo(email, credentials, ipAddresses, "true");
    }

    public void updateAccountInfo(String email, String credentials, String ipAddresses, String validated) throws SQLException {
        execute(SYSADMIN, "Accounts" + UPDATE_FOLLOWER, new Object[]{credentials, ipAddresses, validated, email}, getKey());
    }

    public List<Row> getGroups(String email) throws SQLException {
        return execute(SYSADMIN, "Account-Group-Xref" + SELECT_FOLLOWER, new Object[]{email}, getKey());
    }

    public void addGroupToAccount(String group, String email) throws SQLException {
        execute(SYSADMIN, "Account-Group-Xref" + INSERT_FOLLOWER, new Object[]{
                email,
                group
        }, getKey());
    }
}
