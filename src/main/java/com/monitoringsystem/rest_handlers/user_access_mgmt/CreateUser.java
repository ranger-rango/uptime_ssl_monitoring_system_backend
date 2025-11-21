package com.monitoringsystem.rest_handlers.user_access_mgmt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.TokenGenerator;
import com.monitoringsystem.utils.hashing.Hasher;
import com.monitoringsystem.utils.notification_manager.Mailer;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/uam", templatePath = "/user", httpMethod = "POST")
public class CreateUser implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        // role_id(FK), channel_id(FK), email_address, tel, telegram<NULL>, first_name, middle_name, surname, password, status<ACTIVE, PENDING, INACTIVE>
        FormParserFactory formFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formFactory.createParser(httpServerExchange);

        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("Error !!!");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        Integer roleId = Integer.parseInt(formData.getFirst("role_id").getValue()); // role_title TODO
        Integer channelId = Integer.parseInt(formData.getFirst("channel_id").getValue()); // channel_title TODO
        String emailAddress = formData.getFirst("email_address").getValue();
        String tel = formData.getFirst("tel").getValue();
        String telegram = formData.getFirst("telegram").getValue();
        String firstName = formData.getFirst("first_name").getValue();
        String middleName = formData.getFirst("middle_name").getValue();
        String surname = formData.getFirst("surname").getValue();
        String password = "NULL";
        String status = "PENDING";
        String userId = Hasher.hashSHA512(TokenGenerator.generateSecureToken());

        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
            INSERT INTO system_users (user_id, role_id, channel_id, email_address, tel, telegram, first_name, middle_name, surname, password, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        List<Object> sqlParams = List.of(userId, roleId, channelId, emailAddress, tel, telegram, firstName, middleName, surname, password, status);
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet);
            String registrationUrl = String.format("http://localhost:9090/api/uam/auth/register/%s", TokenGenerator.generateSecureToken());
            Mailer.sendResgistrationUrl("user-registration-notification", emailAddress, firstName, registrationUrl);
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
