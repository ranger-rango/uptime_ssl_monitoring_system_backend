package com.monitoringsystem.rest_handlers.user_access_mgmt.auth;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class Rbac implements HttpHandler
{
    private final HttpHandler next;
    private final String[] allowedRoles;

    public Rbac(HttpHandler next, String... allowedRoles)
    {
        this.next = next;
        this.allowedRoles = allowedRoles;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        UserSession userSession = httpServerExchange.getAttachment(Attachments.USER_SESSION);
        if (userSession == null)
        {
            httpServerExchange.setStatusCode(401);
            httpServerExchange.getResponseSender().send("{'error': 'Session Error'}");
            return;
        }

        final String[] userRole = new String[1];
        try
        {
            userRole[0] = userSession.userRole().toUpperCase();
        }
        catch(NullPointerException e)
        {
            userRole[0] = "NULL";
        }

        // Arrays.stream(allowedRoles).forEach(role -> 
        //     {
        //         if (userRole[0].equals(role.toUpperCase()))
        //         {
        //             try
        //             {
        //                 next.handleRequest(httpServerExchange);
        //             }
        //             catch (Exception e)
        //             {
        //                 e.printStackTrace();
        //             }
        //             return;
        //         }
        //     }
        // );
        System.out.println("userRole" + userRole[0]);
        for (String allowedRole : allowedRoles)
        {
            System.out.println(allowedRole);
            if (userRole[0].equals(allowedRole.toUpperCase()))
            {
                try
                {
                    next.handleRequest(httpServerExchange);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
        }

        httpServerExchange.setStatusCode(413);
        httpServerExchange.getResponseSender().send("{'error': 'Forbidden'}");

    }

    

}
