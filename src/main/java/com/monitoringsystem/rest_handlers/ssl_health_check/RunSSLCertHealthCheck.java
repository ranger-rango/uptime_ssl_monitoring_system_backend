package com.monitoringsystem.rest_handlers.ssl_health_check;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monitoringsystem.rest_handlers.domain_health_check.utils.DomainHealthCheckUtils;
import com.monitoringsystem.utils.Constants;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.Logger;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/services", templatePath = "/ssl-cert-health-check/{serviceId}", httpMethod = "GET")
public class RunSSLCertHealthCheck implements HttpHandler
{
    private static LocalDate certExpiryDate;
    private static String domain;
    private static LocalDateTime dateTimeToday;
    public static long usingDaysToExpiry()
    {
        LocalDate dateToday = LocalDate.now();
        dateTimeToday = LocalDateTime.now();
        return ChronoUnit.DAYS.between(dateToday, certExpiryDate);
    }

    public static boolean usingSslHandshake()
    {
        dateTimeToday = LocalDateTime.now();
        return DomainHealthCheckUtils.validateSslCertificateAndHandshake(domain);
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String serviceId = httpServerExchange.getQueryParameters().get("serviceId").getFirst();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Map<String, Object>> serviceConfigs = Constants.SERVICE_CONFIGURATION;
        Map<String, Object> serviceConfig = serviceConfigs.entrySet().stream()
        .filter(entry -> String.valueOf(entry.getValue().get("service_id")).equals(serviceId))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);
        // String certExpiryDateString = String.valueOf(serviceConfig.get("expiry_date"));
        Object expiryDateObj = serviceConfig.get("expiry_date");
        long expiryTimestamp = 0;
        if (expiryDateObj instanceof Number)
        {
            expiryTimestamp = ((Number) expiryDateObj).longValue();
        }
        else
        {
            expiryTimestamp = Long.parseLong(String.valueOf(expiryDateObj));
        }
        Integer alertThreshold = (Integer) serviceConfig.get("alert_threshold_days");
        System.out.println("alertThresholdDays: " + alertThreshold);
        domain = String.valueOf(serviceConfig.get("service_url_domain"));
        // certExpiryDate = LocalDate.parse(certExpiryDateString);
        certExpiryDate = java.time.Instant.ofEpochMilli(expiryTimestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        String certId = (String) serviceConfig.get("cert_id");

        Map<String, Object> responseMap = new HashMap<>();

        long daysDifference = usingDaysToExpiry();
        if (daysDifference == alertThreshold)
        {
            responseMap.put("cert_health_status", "WATCH");
        }
        if (daysDifference > alertThreshold)
        {
            responseMap.put("cert_health_status", "VALID");
        }
        if (daysDifference < alertThreshold)
        {
            responseMap.put("cert_health_status", "EXPIRED");
        }

        responseMap.put("check_timestamp", dateTimeToday.toString());
        String response = objectMapper.writeValueAsString(responseMap);

        List<Object> sqlParams = List.of(
            certId,
            responseMap.get("cert_health_status"),
            dateTimeToday
        );
        Logger.certHealthCheckLogger(sqlParams);

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);

    }
}
