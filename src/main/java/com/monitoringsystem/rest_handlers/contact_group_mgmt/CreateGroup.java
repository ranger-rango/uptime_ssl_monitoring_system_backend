package com.monitoringsystem.rest_handlers.contact_group_mgmt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.hashing.Hasher;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/contact-group", templatePath = "", httpMethod = "POST")
public class CreateGroup implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        FormParserFactory formParserFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formParserFactory.createParser(httpServerExchange);
        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("Error !!!");
            return;
        }

        FormData formData = formDataParser.parseBlocking();
        String groupName = formData.getFirst("group_name").getValue();
        String description = formData.getFirst("description").getValue();
        String contactGroupId = Hasher.hashSHA512(groupName);
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            INSERT INTO contact_group (contact_group_id, group_name, description) 
            VALUES (?, ?, ?)
            """;
        
        List<Object> sqlParams = List.of(contactGroupId, groupName, description);
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet);
        }
        else
        {
            response = "{'status': 'success'}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}