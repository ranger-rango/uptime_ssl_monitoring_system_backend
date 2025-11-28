package com.monitoringsystem.rest_handlers.domain_health_check.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DomainHealthCheckUtils
{
    public static String getErrorMessage(HttpURLConnection connection)
    {
        try (InputStream errorStream = connection.getErrorStream())
        {    
            if (errorStream == null)
            {
                return "No error body provided by server.";
            }            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream)))
            {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                {
                    response.append(line.trim());
                }
                return response.toString();
            }
        }
        catch (IOException e)
        {
            return "Error reading error stream: " + e.getMessage();
        }
    }

    public static Map<String, Object> checkIcmpReachability(String host)
    {
        Map<String, Object> serviceStatusMap = new HashMap<>();
        LocalDateTime testTime;
        try
        {
            InetAddress address = InetAddress.getByName(host);
            address.isReachable(5000);
            testTime = LocalDateTime.now();
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "UP");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        catch (IOException e)
        {
            testTime = LocalDateTime.now();
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "DOWN");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
    }

    public static Map<String, Object> checkFullPageLoad(String urlString) throws URISyntaxException
    {
        Map<String, Object> serviceStatusMap = new HashMap<>();
        LocalDateTime testTime;
        HttpURLConnection connection = null;
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            long startTime = System.currentTimeMillis();
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            System.out.println("dhcutil: " + responseCode);
            long endTime = System.currentTimeMillis();
            testTime = LocalDateTime.now();
            long responseTime = endTime - startTime;

            String errBody = "";
            if (responseCode >= 400)
            {
                errBody = getErrorMessage(connection);
            }
            serviceStatusMap.put("response_code", responseCode);
            serviceStatusMap.put("response_time_ms", responseTime);
            serviceStatusMap.put("error_message", errBody);
            serviceStatusMap.put("svc_health_status", "UP");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        catch (IOException e)
        {
            testTime = LocalDateTime.now();
            System.err.println("GET Request failed for " + urlString + ": " + e.getMessage());
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "DOWN");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    public static Map<String, Object> checkTcpPortConnectivity(String host, int port)
    {
        Map<String, Object> serviceStatusMap = new HashMap<>();
        LocalDateTime testTime = LocalDateTime.now();
        try (Socket socket = new Socket())
        {
            socket.connect(new java.net.InetSocketAddress(host, port), 5000);
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", 0);
            serviceStatusMap.put("svc_health_status", "UP");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        catch (IOException e)
        {
            testTime = LocalDateTime.now();
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "DOWN");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
    }

    public static X509Certificate[] getSslCertificateAndHandshake(String urlString)
    {
        HttpsURLConnection connection = null;
        X509Certificate[] certs = null;
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect(); 

            certs = (X509Certificate[]) connection.getServerCertificates();
        }
        catch (Exception e)
        {
            System.err.println("TLS Handshake failed for " + urlString + ": " + e.getMessage());
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
        return certs;
    }

    public static boolean validateSslCertificateAndHandshake(String urlStrig)
    {
        X509Certificate domainCert = getSslCertificateAndHandshake(urlStrig)[0];
        Date expiryDate = domainCert.getNotAfter();
        Date currentDate = new Date();
        return currentDate.compareTo(expiryDate) < 0;
    }

    public static Map<String, Object> headRequest(String urlString) throws URISyntaxException
    {
        Map<String, Object> serviceStatusMap = new HashMap<>();
        LocalDateTime testTime = LocalDateTime.now();
        HttpURLConnection connection = null;
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            long startTime = System.currentTimeMillis();
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            String errBody = "";
            if (responseCode >= 400)
            {
                errBody = getErrorMessage(connection);
            }
            serviceStatusMap.put("response_code", responseCode);
            serviceStatusMap.put("response_time_ms", responseTime);
            serviceStatusMap.put("error_message", errBody);
            serviceStatusMap.put("svc_health_status", "UP");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        catch (IOException e)
        {
            testTime = LocalDateTime.now();
            System.err.println("GET Request failed for " + urlString + ": " + e.getMessage());
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "DOWN");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    public static Map<String, Object> optionsRequest(String urlString) throws URISyntaxException
    {
        Map<String, Object> serviceStatusMap = new HashMap<>();
        LocalDateTime testTime = LocalDateTime.now();
        HttpURLConnection connection = null;
        try
        {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("OPTIONS");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            long startTime = System.currentTimeMillis();
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            String errBody = "";
            if (responseCode >= 400)
            {
                errBody = getErrorMessage(connection);
            }
            serviceStatusMap.put("response_code", responseCode);
            serviceStatusMap.put("response_time_ms", responseTime);
            serviceStatusMap.put("error_message", errBody);
            serviceStatusMap.put("svc_health_status", "UP");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        catch (IOException e)
        {
            testTime = LocalDateTime.now();
            System.err.println("GET Request failed for " + urlString + ": " + e.getMessage());
            serviceStatusMap.put("response_code", 0);
            serviceStatusMap.put("response_time_ms", 0);
            serviceStatusMap.put("error_message", "");
            serviceStatusMap.put("svc_health_status", "DOWN");
            serviceStatusMap.put("check_at", testTime);
            return serviceStatusMap;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }
    
}
