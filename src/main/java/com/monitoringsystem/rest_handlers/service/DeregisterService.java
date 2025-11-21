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
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/service", templatePath = "", httpMethod = "DELETE")
public class DeregisterService implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        FormParserFactory formFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formFactory.createParser(httpServerExchange);

        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("Error !!!");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        String serviceId = formData.getFirst("service_id").getValue();
        String status = "TERMINAL";

        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            UPDATE service_info
            SET svc_registration_status = ?
            WHERE service_id = ?
            """;
        
        List<Object> sqlParams = List.of(serviceId, status);
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
