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

@EndpointProps(prefixPath = "/contact-group", templatePath = "/members", httpMethod = "POST")
public class GetGroupMembers implements HttpHandler
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
            SELECT cgm.user_id, su.user_id, su.role_id, su.channel_id, su.email_address, su.first_name, su.surname
            FROM contact_group_members cgm
            JOIN system_users su ON su.user_id = cgm.user_id
            WHERE contact_group_id = ?
            """;
        List<Object> sqlParams = List.of(contactGroupId);
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
