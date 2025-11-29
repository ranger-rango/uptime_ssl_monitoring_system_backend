package com.monitoringsystem.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HttpRequestHandler
{
    public static String fetchRequest(URI uri)
    {
        HttpURLConnection connection = null;
        StringBuilder content = new StringBuilder();
        try
        {
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Key", Constants.DEFAULT_ADMIN_AUTH_TOKEN);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            Integer responseCode = connection.getResponseCode();
            
            // if (responseCode >= 200 && responseCode < 300)
            if (responseCode != null)
            {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));)
                {
                    String inputLine;
                    
                    while ((inputLine = bufferedReader.readLine()) != null)
                    {
                        content.append(inputLine);    
                    }
                }
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return content.toString();
    }
}
