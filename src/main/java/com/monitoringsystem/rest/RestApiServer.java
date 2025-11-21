package com.monitoringsystem.rest;

import java.nio.charset.StandardCharsets;

import com.monitoringsystem.rest.utils.CORSHandler;
import com.monitoringsystem.utils.Constants;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;

public class RestApiServer
{

    private static Undertow server;
    public static Undertow restApiServer()
    {
        String baseApiPath = Constants.getUndertowBasePathRest();
        String undertowHost = Constants.getUndertowHost();
        Integer undertowPort = Integer.parseInt(Constants.getUndertowPort());

        RoutingHandler apiRoutes = Routes.routesRegister();
        PathHandler pathHandler = Handlers.path().addPrefixPath(baseApiPath, apiRoutes);

        server = Undertow.builder()
        .setServerOption(UndertowOptions.DECODE_URL, true)
        .setServerOption(UndertowOptions.URL_CHARSET, StandardCharsets.UTF_8.name())
        .setIoThreads(10)
        .setWorkerThreads(100)
        .addHttpListener(undertowPort, undertowHost)
        .setHandler(new CORSHandler(pathHandler))
        .build();

        return server;

    }

    public static void restApiServerStart()
    {
        Runnable undertowServerThread = () -> 
        {
            try 
            {
                if (server == null)
                {
                    restApiServer();
                }
                server.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        new Thread(undertowServerThread, "undertow-server-thread").start();

    }

    public static void restApiServerStop()
    {
        server.stop();
    }
}
