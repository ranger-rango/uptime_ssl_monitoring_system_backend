package com.monitoringsystem.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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
        Path baseDir = Paths.get("target/classes/com/monitoringsystem/rest_handlers");
        String basePackage = "com.monitoringsystem.rest_handlers";
        RoutingHandler handlers = Handlers.routing();

        try (Stream<Path> files = Files.walk(baseDir))
        {
            files.filter(p -> p.toString().endsWith(".class")).forEach(path -> 
            {
                String relativePath = baseDir.relativize(path).toString();
                String className = basePackage + "." + relativePath.replace("/", ".").replace("\\", ".").replace(".class", "");
                
                try
                {
                    Class<?> handlerClass = Class.forName(className);
                    if (handlerClass.isAnnotationPresent(EndpointProps.class))
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
                        System.out.println(fullPath);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return handlers;
    }
}
