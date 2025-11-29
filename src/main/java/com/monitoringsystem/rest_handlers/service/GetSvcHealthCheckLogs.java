package com.monitoringsystem.rest_handlers.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/services", templatePath = "/health-check-logs", httpMethod = "GET", allowedRoles = {"ADMIN", "OPERATOR", "VIEWER"})
public class GetSvcHealthCheckLogs implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            SELECT shcl.shc_log_id, shcl.service_id, ss.svc_health_status, shcl.response_time_ms, shcl.http_response_code, shcl.check_at, shcl.error_message
            FROM service_health_check_logs shcl
            JOIN service_status ss ON shcl.svc_status_id = ss.svc_status_id
            """;
        List<Object> sqlParams = List.of();
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
        }
        else
        {
            response = "{\"err_status\" : \"Services health check logs fetch failed\"}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}
