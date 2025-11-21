package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@EndpointProps(prefixPath = "/uam", templatePath = "/auth/login", httpMethod = "POST")
public class LoginUser implements HttpHandler
{
    @SuppressWarnings("unchecked")
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
        String emailAddress = formData.getFirst("email_address").getValue();
        String password = formData.getFirst("password").getValue();
        String hashedPassword = Hasher.hashSHA512(password);
        Boolean rememberMe = formData.getFirst("remember_me").getValue() != null; 

        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
        SELECT su.user_id, rl.role_title, nc.channel_title, su.email_address, su.tel, su.telegram, su.first_name, su.middle_name, su.surname
        FROM system_users su
		JOIN roles rl ON rl.role_id = su.role_id
		JOIN notification_channels nc ON nc.channel_id = su.channel_id
        WHERE email_address = ? AND password = ?
            """;
        List<Object> sqlParams = List.of(emailAddress, hashedPassword);
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        String authToken = "";
        Map<String, Map<String, Object>> map = new HashMap<>();
        if (resultSet != null)
        {
            String jsonString = DatabaseResultsProcessors.processResultsToJson(resultSet);
            ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.readValue(jsonString, Map.class);
            Map<String, Object> userDetails = map.get("1");
            if (rememberMe)
            {
                authToken = AuthManager.evergreenAuthToken(emailAddress);
            }
            else
            {
                authToken = AuthManager.ephemeralAuthToken(emailAddress);
            }
            userDetails.put(emailAddress, authToken);
            response = objectMapper.writeValueAsString(map);
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
