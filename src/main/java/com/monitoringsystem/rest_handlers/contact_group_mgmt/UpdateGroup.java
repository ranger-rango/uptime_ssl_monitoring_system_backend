package com.monitoringsystem.rest_handlers.contact_group_mgmt;

import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

@EndpointProps(prefixPath = "/contact-group", templatePath = "/update", httpMethod = "POST")
public class UpdateGroup implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {}
}
