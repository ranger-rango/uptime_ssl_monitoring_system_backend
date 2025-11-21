package com.monitoringsystem.rest_handlers.service;

import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

@EndpointProps(prefixPath = "/service", templatePath = "/update", httpMethod = "POST")
public class UpdateService implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {}
}
