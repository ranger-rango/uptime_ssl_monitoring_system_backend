package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

@EndpointProps(prefixPath = "/uam", templatePath = "/auth/register/{registrationToken}", httpMethod = "POST", allowedRoles = {"GUEST"})
public class RegisterUser implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String registrationToken = httpServerExchange.getQueryParameters().get("registrationToken").getFirst();
        FormParserFactory formFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formFactory.createParser(httpServerExchange);

        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("{\"err_status\": \"Registration failed\"}");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        String emailAddress = formData.getFirst("email_address").getValue();
        String password = formData.getFirst("password").getValue();
        String passwordConfirm = formData.getFirst("password_confirm").getValue();
        String status = "ACTIVE";
        if (!"".equals(emailAddress) && !"".equals(password) && !"".equals(passwordConfirm))
        {
            if (!ValidateRegToken.isValid(emailAddress, registrationToken))
            {
                httpServerExchange.setStatusCode(400);
                httpServerExchange.getResponseSender().send("{'status': 'error'}");
                return;
            }

            if (password.equals(passwordConfirm))
            {
                String hashedPassword = Hasher.hashSHA512(password);
                Connection connection;
                try
                {
                    connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
                    String sqlQuery = """
                    UPDATE system_users
                    SET password = ?,
                        status = ?
                    WHERE email_address = ?
                        """;
                    List<Object> sqlParams = List.of(hashedPassword, status, emailAddress);
                    ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
                    String response = "";
                    if (resultSet != null && resultSet.next())
                    {
                        resultSet.beforeFirst();
                        response = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
                    }
                    else
                    {
                        response = "{\"status\": \"Registration successful\"}";
                        AuthManager.destroyToken(emailAddress, "REG_TOKEN");
                    }

                    httpServerExchange.setStatusCode(200);
                    httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    httpServerExchange.getResponseSender().send(response);
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
                httpServerExchange.getResponseSender().send("{\"err_status\": \"Registration Failed\"}");
            }
        }
        else
        {
            httpServerExchange.setStatusCode(200);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"Registration Failed\"}");
        }


    }

}
