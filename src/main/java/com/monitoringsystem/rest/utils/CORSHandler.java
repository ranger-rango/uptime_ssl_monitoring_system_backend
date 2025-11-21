package com.monitoringsystem.rest.utils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

public class CORSHandler implements HttpHandler
{

    private final HttpHandler httpHandler;

    public CORSHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        httpServerExchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
        httpServerExchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "POST, GET, OPTIONS, PUT, PATCH, DELETE");
        httpServerExchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "AuthToken,RequestReference");

        if (httpHandler != null) {
            httpHandler.handleRequest(httpServerExchange);
        }
    }
}