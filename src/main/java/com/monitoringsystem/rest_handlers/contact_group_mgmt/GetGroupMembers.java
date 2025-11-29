package com.monitoringsystem.rest_handlers.contact_group_mgmt;

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

@EndpointProps(prefixPath = "/contact-group", templatePath = "/members/{groupId}", httpMethod = "GET", allowedRoles = {"ADMIN", "OPERATOR"})
public class GetGroupMembers implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String contactGroupId = httpServerExchange.getQueryParameters().get("groupId").getFirst();

        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            SELECT cgm.user_id, rl.role_title, nc.channel_title, su.email_address, su.tel, su.telegram, su.first_name, su.surname
            FROM contact_group_members cgm
            JOIN system_users su ON su.user_id = cgm.user_id
            JOIN roles rl ON rl.role_id = su.role_id
            JOIN notification_channels nc ON nc.channel_id = su.channel_id
            WHERE contact_group_id = ?
            """;
        List<Object> sqlParams = List.of(contactGroupId);
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
        }
        else
        {
            response = "{\"status\" : \"Fetch group members failed\"}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}
