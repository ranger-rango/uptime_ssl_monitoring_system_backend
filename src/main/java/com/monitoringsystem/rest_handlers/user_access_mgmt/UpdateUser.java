package com.monitoringsystem.rest_handlers.user_access_mgmt;

import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

@EndpointProps(prefixPath = "/uam", templatePath = "/user/update", httpMethod = "POST")
public class UpdateUser implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {}
}
