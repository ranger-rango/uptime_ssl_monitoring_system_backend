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

@EndpointProps(prefixPath = "/contact-group", templatePath = "/{contactGroupId}", httpMethod = "GET")
public class GetGroup implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String contactGroupId = httpServerExchange.getQueryParameters().get("contactGroupId").getFirst();
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
        SELECT contact_group_id, group_name, description
        FROM contact_groups
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
            response = "{'status' : 'error'}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}
