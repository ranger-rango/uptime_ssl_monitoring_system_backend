package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.TokenGenerator;

public class AuthManager
{
    @SuppressWarnings("unchecked")
    public static String fetchAuthToken(String emailAddress)
    {
        String storedToken = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection())
        {
            String sqlQuery = """
                    SELECT auth_token
                    FROM user_auth_tokens
                    WHERE email_address = ?
                    """;
            List<Object> sqlParams = List.of(emailAddress);
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet != null)
            {
                String jsonString = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
                Map<String, Map<String, Object>> resultMap = objectMapper.readValue(jsonString, Map.class);
                Map<String, Object> innerMap = resultMap.get("1");
                storedToken = innerMap != null ? String.valueOf(resultMap.get("1").get("auth_token")) : null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return storedToken;

    }

    public static void insertToken(String emailAddress, String token, String tokenType)
    {
        String sqlQuery = "";
        if ("AUTH_TOKEN".equals(tokenType))
        {
            sqlQuery = """
                    INSERT INTO user_auth_tokens (email_address, auth_token)
                    VALUES (?, ?)
                    """;
        }
        if ("REG_TOKEN".equals(tokenType))
        {
           sqlQuery = """
                    INSERT INTO user_registration_tokens (email_address, registration_token)
                    VALUES (?, ?)
                    """;
        }
        try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection())
        {
            List<Object> sqlParams = List.of(emailAddress, token);
            DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String ephemeralAuthToken(String emailAddress)
    {
        String storedToken = fetchAuthToken(emailAddress);
        if (storedToken != null)
        {
            return storedToken;
        }
        else
        {
            String secureToken = TokenGenerator.generateSecureToken();
            String authToken = "session:" + secureToken;
            insertToken(emailAddress, authToken, "AUTH_TOKEN");
            return authToken;
        }
    }

    public static String evergreenAuthToken(String emailAddress)
    {
        String storedToken = fetchAuthToken(emailAddress);
        if (storedToken != null)
        {
            return storedToken;
        }
        else
        {
            String secureToken = TokenGenerator.generateSecureToken();
            String authToken = "local:" + secureToken;
            insertToken(emailAddress, authToken, "AUTH_TOKEN");
            return authToken;
        }
    }

    public static void destroyToken(String emailAddress, String tokenType)
    {
        String sqlQuery = "";
        if ("AUTH_TOKEN".equals(tokenType))
        {
            sqlQuery = """
                    DELETE FROM user_auth_tokens
                    WHERE email_address = ?
                    """;
        }
        if ("REG_TOKEN".equals(tokenType))
        {
           sqlQuery = """
                    DELETE FROM user_registration_tokens
                    WHERE email_address = ?
                    """;
        }
        try(Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();)
        {
            List<Object> sqlParams = List.of(emailAddress);
            DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
