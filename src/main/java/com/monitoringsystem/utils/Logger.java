package com.monitoringsystem.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;

public class Logger
{
    public static void loggerHelper(String sqlQuery, List<Object> sqlParams)
    {
        Connection connection = null;
        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
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

    public static void svcCheckLogger(List<Object> sqlParams)
    {
        String sqlQuery = """
                INSERT INTO service_health_check_logs (service_id, svc_status_id, response_time_ms, http_response_code, check_at, error_message) 
                VALUES (?, 
                (SELECT svc_status_id FROM service_status WHERE svc_health_status = ? LIMIT 1), 
                ?, ?, ?, ?)
                """;
        loggerHelper(sqlQuery, sqlParams);
    }

    public static void certHealthCheckLogger(List<Object> sqlParams)
    {
        String sqlQuery = """
                INSERT INTO ssl_cert_health_check_logs (cert_id, cert_status_id, last_checked_timestamp) 
                VALUES (
                ?, 
                (SELECT cert_status_id FROM certificate_status WHERE cert_health_status = ? LIMIT 1), 
                ?)
                """;
        loggerHelper(sqlQuery, sqlParams);
    }

    public static void userActionsLogger(List<Object> sqlParams)
    {
        String sqlQuery = """
                INSERT INTO user_actions_audit_logs (uaa_log_id, user_id, service_id, action) 
                VALUES (?, ?, ?, ?)
                """;
        loggerHelper(sqlQuery, sqlParams);
    }
    
    public static void notificationLogger(List<Object> sqlParams)
    {
        String sqlQuery = """
                INSERT INTO notification_history_logs (notification_trigger_id, user_id, channel_id, created_at) 
                VALUES (?, ?, ?, ?)
                """;
        loggerHelper(sqlQuery, sqlParams);
    }
    
}
