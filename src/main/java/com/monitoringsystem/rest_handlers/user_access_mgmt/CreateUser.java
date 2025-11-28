package com.monitoringsystem.rest_handlers.user_access_mgmt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.rest_handlers.user_access_mgmt.auth.AuthManager;
import com.monitoringsystem.utils.Constants;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.TokenGenerator;
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
    private static String frontendDomain = Constants.FRONTEND_DOMAIN;
    private static String frontendRegUserEndpoint = Constants.FRONTEND_REG_USER_ENDPOINT;
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        FormParserFactory formFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formFactory.createParser(httpServerExchange);

        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"User creation failed\"}");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        String roleTitle = formData.getFirst("role_title").getValue();
        String channelTitle = formData.getFirst("channel_title").getValue();
        String emailAddress = formData.getFirst("email_address").getValue();
        String tel = formData.getFirst("tel").getValue();
        String telegram = formData.getFirst("telegram").getValue();
        String firstName = formData.getFirst("first_name").getValue();
        String middleName = formData.getFirst("middle_name").getValue();
        String surname = formData.getFirst("surname").getValue();
        String password = "NULL";
        String status = "PENDING";
        String userId = TokenGenerator.generateSecureToken();
        if (!"".equals(roleTitle) && !"".equals(channelTitle) && !"".equals(emailAddress) && !"".equals(tel) && !"".equals(telegram) && !"".equals(firstName) && !"".equals(middleName) && !"".equals(surname))
        {

            Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            String sqlQuery = """
                INSERT INTO system_users (user_id, role_id, channel_id, email_address, tel, telegram, first_name, middle_name, surname, password, status) 
                VALUES (?, 
                (SELECT role_id FROM roles WHERE role_title = ?), 
                (SELECT channel_id FROM notification_channels WHERE channel_title = ?), 
                ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            List<Object> sqlParams = List.of(userId, roleTitle, channelTitle, emailAddress, tel, telegram, firstName, middleName, surname, password, status);
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            String response = "";
            if (resultSet != null && resultSet.next())
            {
                resultSet.beforeFirst();
                response = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
            }
            else
            {
                String regToken = TokenGenerator.generateSecureToken();
                String registrationUrl = String.format("%s%s/%s", frontendDomain, frontendRegUserEndpoint, regToken);
                Mailer.sendResgistrationUrl("user-registration-notification.html", emailAddress, firstName, registrationUrl);
                AuthManager.insertToken(emailAddress, regToken, "REG_TOKEN");
                response = "{\"status\": \"User creation successful\"}";
            }

            httpServerExchange.setStatusCode(200);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            httpServerExchange.getResponseSender().send(response);
        }
        else
        {
            httpServerExchange.setStatusCode(200);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"User creation failed\"}");
        }

    }

}
