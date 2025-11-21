package com.monitoringsystem.rest_handlers.user_access_mgmt;

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

@EndpointProps(prefixPath = "/uam", templatePath = "/users", httpMethod = "GET")
public class GetUsers implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
        SELECT su.user_id, rl.role_title, nc.channel_title, su.email_address, su.tel, su.telegram, su.first_name, su.middle_name, su.surname
        FROM system_users su
		JOIN roles rl ON rl.role_id = su.role_id
		JOIN notification_channels nc ON nc.channel_id = su.channel_id
            """;
        List<Object> sqlParams = List.of();
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet);
        }
        else
        {
            response = "{'status' : 'error'}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}
