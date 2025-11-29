package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/uam", templatePath = "/auth/logout/{userId}", httpMethod = "POST", allowedRoles = {"ADMIN", "OPERATOR", "VIEWER"})
public class Logout implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String userId = httpServerExchange.getQueryParameters().get("userId").getFirst();
        if (!"".equals(userId))
        {
            try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();)
            {
                
                String sqlQuery = """
                    SELECT email_address FROM system_users where user_id = ?
                    """;
                List<Object> sqlParams = List.of(userId);
                ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
                if (resultSet != null && resultSet.next())
                {
                    String emailAddress = resultSet.getString("email_address");
                    if (emailAddress.equals("default.system@ssldom.com"))
                    {
                        return;
                    }
                    AuthManager.destroyToken(emailAddress, "AUTH_TOKEN");

                    httpServerExchange.setStatusCode(200);
                    httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    httpServerExchange.getResponseSender().send("{\"status\" : \"Logout Successful\"}");
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            httpServerExchange.setStatusCode(200);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"Logout Failed\"}");
        }

    }
}
