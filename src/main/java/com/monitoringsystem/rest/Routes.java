package com.monitoringsystem.rest;

import org.reflections.Reflections;

import com.monitoringsystem.rest_handlers.user_access_mgmt.auth.ApiKeyAuthHandler;
import com.monitoringsystem.rest_handlers.user_access_mgmt.auth.Rbac;
import com.monitoringsystem.utils.EndpointProps;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;

public class Routes
{
    public static RoutingHandler routesRegister()
    {
        RoutingHandler handlers = Handlers.routing();

        Reflections reflections = new Reflections("com.monitoringsystem.rest_handlers");

        reflections.getTypesAnnotatedWith(EndpointProps.class).forEach(handlerClass -> 
        {
            try
            {
                EndpointProps endpointProps = handlerClass.getDeclaredAnnotation(EndpointProps.class);
                String endpointPrefixPath = endpointProps.prefixPath();
                String endpointTemplatePath = endpointProps.templatePath();
                String fullPath = endpointPrefixPath + endpointTemplatePath;
                fullPath = fullPath.replaceAll("//", "/");
                String endpointMethod = endpointProps.httpMethod();
                String[] allowedRoles = endpointProps.allowedRoles();

                HttpHandler handlerInstance = (HttpHandler) handlerClass.getDeclaredConstructor().newInstance();

                switch (endpointMethod)
                {
                    case "GET" -> handlers.get(fullPath, new BlockingHandler(new ApiKeyAuthHandler(new Rbac(handlerInstance, allowedRoles))));
                    case "POST" -> handlers.post(fullPath, new BlockingHandler(new ApiKeyAuthHandler(new Rbac(handlerInstance, allowedRoles))));
                    case "PUT" -> handlers.put(fullPath, new BlockingHandler(new ApiKeyAuthHandler(new Rbac(handlerInstance, allowedRoles))));
                    case "DELETE" -> handlers.delete(fullPath, new BlockingHandler(new ApiKeyAuthHandler(new Rbac(handlerInstance, allowedRoles))));
                    default -> throw new IllegalArgumentException("Unsupported HTTP Method: " + endpointMethod);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });


        return handlers;
    }
}
