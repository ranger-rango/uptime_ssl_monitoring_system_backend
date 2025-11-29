package com.monitoringsystem.rest_handlers.service;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.rest_handlers.domain_health_check.utils.DomainHealthCheckUtils;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.TokenGenerator;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/service", templatePath = "", httpMethod = "POST", allowedRoles = {"ADMIN", "OPERATOR"})
public class RegisterService implements HttpHandler
{
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
        FormParserFactory formFactory = FormParserFactory.builder().build();
        FormDataParser formDataParser = formFactory.createParser(httpServerExchange);

        if (formDataParser == null)
        {
            httpServerExchange.setStatusCode(400);
            httpServerExchange.getResponseSender().send("Error !!!");
            return;
        }
        FormData formData = formDataParser.parseBlocking();
        
        String serviceName = formData.getFirst("service_name").getValue();
        String serviceId = TokenGenerator.generateSecureToken();
        String serviceUrlDomain = formData.getFirst("service_url_domain").getValue();
        Integer servicePort = Integer.parseInt(formData.getFirst("service_port").getValue());
        String serviceRegistrationStatus = "ACTIVE";

        String svcDiagnosisMethod = formData.getFirst("diagnosis_method").getValue();
        Integer svcDiagnosisInterval = Integer.parseInt(formData.getFirst("svc_diagnosis_interval").getValue());
        Integer numOfRetries = Integer.parseInt(formData.getFirst("num_of_retries").getValue());
        Integer retryIntervalSecs = Integer.parseInt(formData.getFirst("retry_interval_secs").getValue());
        String contactGroupId  = formData.getFirst("contact_groups").getValue();

        String certId = null;
        String certIssuer = null;
        Date certExpDate = null;
        Timestamp certExpiryDate = null;
        X509Certificate cert = null;
        try
        {
            cert = DomainHealthCheckUtils.getSslCertificateAndHandshake(serviceUrlDomain)[0];
        }
        catch (Exception e)
        {}
        if (cert != null)
        {
            certId = cert.getSerialNumber().toString(16);
            certIssuer = cert.getIssuerX500Principal().getName();
            certExpDate = cert.getNotAfter();
            certExpiryDate = new Timestamp(certExpDate.getTime());
        }

        String certDiagnosisMethod = "TLS_HANDSHAKE";
        Integer certDiagnosisInterval = Integer.parseInt(formData.getFirst("cert_diagnosis_interval").getValue());
        Integer certAlertThreshold = Integer.parseInt(formData.getFirst("alert_threshold_days").getValue());
        
        String svcInfoSqlQuery = """
                INSERT INTO service_info (service_id, service_name, service_url_domain, service_port, svc_registration_status) 
                VALUES (?, ?, ?, ?, ?)
            """;

        String svcConfSqlQuery = """
                INSERT INTO service_configs (service_id, svc_diag_id, svc_diagnosis_interval, num_of_retries, retry_interval_secs) 
                VALUES (?, 
                (SELECT svc_diag_id FROM service_diagnosis_methods WHERE diagnosis_method = ?), 
                ?, ?, ?)
            """;

        String certInfoSqlQuery = """
                INSERT INTO ssl_certificate_info (service_id, cert_id, issuer, expiry_date, is_cert_active_status) 
                VALUES (?, ?, ?, ?, ?)
            """;

        String certConfSqlQuery = """
                INSERT INTO ssl_certificate_configs (cert_id, svc_diag_id, cert_diagnosis_interval, alert_threshold_days) 
                VALUES (?, 
                (SELECT svc_diag_id FROM service_diagnosis_methods WHERE diagnosis_method = ?), 
                ?, ?)
            """;
        String contactGrpSqlQuery = """
            INSERT INTO service_contact_groups (service_id, contact_group_id) 
            VALUES (?, ?)
            """;
        
        List<Object> svcInfoSqlParams = List.of(serviceId, serviceName, serviceUrlDomain, servicePort, serviceRegistrationStatus);
        List<Object> svcConfSqlParams = List.of(serviceId, svcDiagnosisMethod, svcDiagnosisInterval, numOfRetries, retryIntervalSecs);
        List<Object> contactGrpSqlParams = List.of(serviceId, contactGroupId);

        String response = "{\"err_status\": \"Service registration failed\"}";
        if (cert != null)
        {
            List<Object> certInfoSqlParams = List.of(serviceId, certId, certIssuer, certExpiryDate, "ACTIVE");
            List<Object> certConfSqlParams = List.of(certId, certDiagnosisMethod, certDiagnosisInterval, certAlertThreshold);
            if (dbOp(svcInfoSqlQuery, svcInfoSqlParams) && dbOp(svcConfSqlQuery, svcConfSqlParams) && dbOp(certInfoSqlQuery, certInfoSqlParams) && dbOp(certConfSqlQuery, certConfSqlParams) && dbOp(contactGrpSqlQuery, contactGrpSqlParams))
            {
                response = "{\"status\": \"Serivce registration successful\"}";
            }
        }
        else
        {
            if (dbOp(svcInfoSqlQuery, svcInfoSqlParams) && dbOp(svcConfSqlQuery, svcConfSqlParams) && dbOp(contactGrpSqlQuery, contactGrpSqlParams))
            {
                response = "{\"status\": \"Serivce registration successful\"}";
            }
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);

    }
}
