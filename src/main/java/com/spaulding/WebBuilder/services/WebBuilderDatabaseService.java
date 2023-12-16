package com.spaulding.WebBuilder.services;

import com.spaulding.tools.Archive.Archive;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.SQLException;

@Service
public class WebBuilderDatabaseService extends Archive {
    public static final String ELEMENTS_TABLE = "elements";

    public WebBuilderDatabaseService(JdbcTemplate jdbcTemplate) throws SQLException {
        super("WebBuilderDatabaseService", jdbcTemplate);
    }

    @Override
    protected void setup() throws SQLException {
        setupElementsTable();
    }

    private void setupElementsTable() throws SQLException {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + ELEMENTS_TABLE + " (name VARCHAR PRIMARY KEY, roles VARCHAR, children VARCHAR, type VARCHAR, class VARCHAR, style VARCHAR, properties VARCHAR, innerHTML VARCHAR)");
        Object[] args = new Object[]{
                ELEMENTS_TABLE + SELECT_FOLLOWER,
                "SELECT * FROM " + ELEMENTS_TABLE + " WHERE name = ?",
                1,
                "String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[]{
                ELEMENTS_TABLE + INSERT_FOLLOWER,
                "INSERT OR IGNORE INTO " + ELEMENTS_TABLE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                6,
                "String,String,String,String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);

        args = new Object[]{
                ELEMENTS_TABLE + UPDATE_FOLLOWER,
                "UPDATE " + ELEMENTS_TABLE + " SET roles = ?, children = ?, type = ?, class = ?, style = ?, properties = ?, innerHTML = ? WHERE name = ?",
                7,
                "String,String,String,String,String,String,String"
        };
        execute(SYSADMIN, AUTH_REF_TABLE_INSERT, args);
    }

    public String buildElement(List<String> roles, String elementName) throws SQLException {
        List<Row> rows = execute(Archive.SYSADMIN, ELEMENTS_TABLE + SELECT_FOLLOWER, new Object[]{ elementName });
        if (rows.isEmpty()) {
            return null;
        }

        Row result = rows.get(0);
        String pageRoles = (String) result.getResult(1);
        boolean auth = false;
        for (String role : roles) {
            if (pageRoles.contains(role)) {
                auth = true;
                break;
            }
        }

        if (auth) {
            String type = (String) result.getResult(3);
            String classes = (String) result.getResult(4);
            String style = (String) result.getResult(5);
            String properties = (String) result.getResult(6);
            String innerHTML = (String) result.getResult(7);

            StringBuilder html = new StringBuilder();
            html.append("<");
            html.append(type);

            if (classes != null && !classes.isEmpty()) {
                html.append(" class=\"");
                html.append(classes);
                html.append("\"");
            }

            if (style != null && !style.isEmpty()) {
                html.append(" style=\"");
                html.append(style);
                html.append("\"");
            }

            if (properties != null && !properties.isEmpty()) {
                html.append(" ");
                html.append(properties);
            }

            html.append(">");
            if (classes != null && !classes.isEmpty()) {
                html.append(innerHTML);
            }

            String[] childElements = ((String) result.getResult(2)).split(",");
            for (String childName : childElements) {
                String childHTML = buildElement(roles, childName);
                if (childHTML != null) {
                    html.append(childHTML);
                }
            }

            html.append("</");
            html.append(type);
            html.append(">");

            return html.toString();
        }

        return null;
    }
}
