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
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/contact-group", templatePath = "/services", httpMethod = "POST")
public class GetGroupServices implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        FormParserFactory formParserFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formParserFactory.createParser(httpServerExchange);
        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("{'status': 'error'}");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        String contactGroupId = formData.getFirst("contact_group_id").getValue();
        
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            SELECT scg.service_id, si.service_id, si.service_name, si.service_url_domain, si.service_port, si.svc_registration_status
            FROM service_contact_groups scg
            JOIN service_info si ON si.service_id = scg.service_id
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
