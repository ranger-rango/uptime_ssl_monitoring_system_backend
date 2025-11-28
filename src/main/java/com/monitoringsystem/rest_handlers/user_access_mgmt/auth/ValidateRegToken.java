package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;

public class ValidateRegToken
{
    public static boolean isValid(String emailAddress, String registrationToken)
    {
        boolean isTokenValid = false;
        try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection())
        {
            String sqlQuery = """
                    SELECT urt.registration_token
                    FROM user_registration_tokens urt
                    JOIN system_users su ON su.email_address = urt.email_address
                    WHERE urt.registration_token = ?  AND su.email_address = ? AND su.status = 'PENDING'
                    """;
            List<Object> sqlParams = List.of(registrationToken, emailAddress);
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet != null && resultSet.next())
            {
                if (resultSet.getString("registration_token").equals(registrationToken))
                {
                    isTokenValid = true;
                }
            }

        }
        catch (Exception e )
        {
            e.printStackTrace();
        }

        return isTokenValid;

    }
}
