package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class ApiKeyAuthHandler implements HttpHandler
{
    private final HttpHandler next;

    public ApiKeyAuthHandler(HttpHandler next)
    {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String authToken = httpServerExchange.getRequestHeaders().getFirst("X-Auth-Key");

        if (authToken == null || authToken.isBlank())
        {
            httpServerExchange.setStatusCode(401);
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"Missing Auth Token\"}");
            return;
        }

        if ("GUEST".equals(authToken))
        {
            UserSession userSession = new UserSession("GUEST", "GUEST", "GUEST");
            httpServerExchange.putAttachment(Attachments.USER_SESSION, userSession);
            next.handleRequest(httpServerExchange);
            return;
        }

        String[] userAttr = UserRepository.lookupUserAttrByApiKey(authToken);

        if (userAttr == null)
        {
            httpServerExchange.setStatusCode(401);
            httpServerExchange.getResponseSender().send("{\"err_status\" : \"Invalid Auth Token\"}");
            return;
        }

        String[] tokenParams = authToken.split(":");
        String userId = userAttr[0];
        String tokenType = tokenParams[0];
        String userRole = userAttr[1];
        UserSession userSession = new UserSession(userId, userRole, tokenType);

        httpServerExchange.putAttachment(Attachments.USER_SESSION, userSession);
        next.handleRequest(httpServerExchange);

    }
}
