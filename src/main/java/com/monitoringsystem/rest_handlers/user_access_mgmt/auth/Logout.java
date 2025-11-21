package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

@EndpointProps(prefixPath = "/uam", templatePath = "/auth/logout", httpMethod = "POST")
public class Logout implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {}
}
