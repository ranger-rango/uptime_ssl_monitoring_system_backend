package com.monitoringsystem.rest_handlers.ssl_health_check;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
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

    public static boolean dbOp(String sqlQuery, List<Object> sqlParams)
    {
        boolean status = false;
        try (Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();)
        {
            
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet == null)
            {
                status = true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return status;
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
        String certId = (String) serviceConfig.get("cert_id");
        if (certId == null)
        {
            return;
        }
        String certExpiryDateString = String.valueOf(serviceConfig.get("expiry_date"));
        Integer alertThreshold = (Integer) serviceConfig.get("alert_threshold_days");
        domain = String.valueOf(serviceConfig.get("service_url_domain"));
        certExpiryDate = LocalDate.parse(certExpiryDateString);

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
            X509Certificate newCert = DomainHealthCheckUtils.getSslCertificateAndHandshake(domain)[0];
            String newCertId = newCert.getSerialNumber().toString(16);
            String newCertIssuer = newCert.getIssuerX500Principal().getName();
            Date newCertExpDate = newCert.getNotAfter();
            Timestamp newCertExpiryDate = new Timestamp(newCertExpDate.getTime());

            if (newCertExpiryDate.after(Date.from(certExpiryDate.atStartOfDay(ZoneId.systemDefault()).toInstant())))
            {
                String certStatus = "TERMINAL";
                String updateCertSqlQuery = """
                        UPDATE ssl_certificate_info
                        SET is_cert_active_status = ?
                        WHERE cert_id = ?
                        """;
                List<Object> updateCertSqlParams = List.of(certStatus, certId);

                String newCertStatus = "ACTIVE";
                String newCertSqlQuery = """
                    INSERT INTO ssl_certificate_info (service_id, cert_id, issuer, expiry_date, is_cert_active_status) 
                    VALUES (?, ?, ?, ?, ?)
                        """;
                List<Object> newCertSqlParams = List.of(serviceId, newCertId, newCertIssuer, newCertExpiryDate, newCertStatus);

                String certDiagnosisMethod = "TLS_HANDSHAKE";
                String certConfSqlQuery = """
                        INSERT INTO ssl_certificate_configs (cert_id, svc_diag_id, cert_diagnosis_interval, alert_threshold_days) 
                        SELECT
                            ?,
                            (SELECT svc_diag_id FROM service_diagnosis_methods WHERE diagnosis_method = ?),
                            cert_diagnosis_interval,
                            alert_threshold_days
                        FROM ssl_certificate_configs
                        WHERE cert_id = ?
                    """;
                List<Object> certConfSqlParams = List.of(newCertId, certDiagnosisMethod, certId);
            if (dbOp(updateCertSqlQuery, updateCertSqlParams) && dbOp(newCertSqlQuery, newCertSqlParams) && dbOp(certConfSqlQuery, certConfSqlParams))
            {
                // String placeholderResponse = "{\"status\": \"Expired SSL Certificate Replaced\"}";
            }
            }
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
