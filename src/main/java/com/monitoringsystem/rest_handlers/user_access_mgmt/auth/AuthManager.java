package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
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
        String storeToken = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection())
        {
            String sqlQuery = """
                    SELECT auth_token
                    FROM user_auth_token
                    WHERE email_address = ?
                    """;
            List<Object> sqlParams = List.of(emailAddress);
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet != null)
            {
                String jsonString = DatabaseResultsProcessors.processResultsToJson(resultSet);
                Map<String, Map<String, Object>> resultMap = objectMapper.readValue(jsonString, Map.class);
                storeToken = String.valueOf(resultMap.get("1").get("auth_token"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return storeToken;

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
            String authToken = "session:" + ":" + secureToken;
            return Base64.getEncoder().encodeToString(authToken.getBytes(StandardCharsets.UTF_8));
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
            String authToken = "local:" + ":" + secureToken;
            return Base64.getEncoder().encodeToString(authToken.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void destroyAuthToken(String emailAddress)
    {
        Connection connection = null;

        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            String sqlQuery = """
            DELETE FROM user_auth_tokens
            WHERE email_address = ?
                """;
            List<Object> sqlParams = List.of(emailAddress);
            DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void storeAuthToken(String emailAddress, String authToken)
    {
        Connection connection = null;

        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            String sqlQuery = """
            INSERT INTO user_auth_tokens (email_address, auth_token)
            VALUES (?, ?)
                """;
            List<Object> sqlParams = List.of(emailAddress, authToken);
            DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

}
