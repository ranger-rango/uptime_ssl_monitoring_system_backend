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

@EndpointProps(prefixPath = "/services", templatePath = "/ssl-check-logs", httpMethod = "GET", allowedRoles = {"ADMIN", "OPERATOR", "VIEWER"})
public class GetCertHealthCheckLogs implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            SELECT schl.schl_log_id, schl.cert_id, cs.cert_health_status, schl.last_checked_timestamp, sci.service_id
            FROM ssl_cert_health_check_logs schl
            JOIN certificate_status cs ON cs.cert_status_id = schl.cert_status_id
            JOIN ssl_certificate_info sci ON sci.cert_id = schl.cert_id
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
            response = "{\"err_status\" : \"Services cert health check logs fetch failed\"}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}

