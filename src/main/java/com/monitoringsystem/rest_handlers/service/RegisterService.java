package com.monitoringsystem.rest_handlers.service;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.rest_handlers.domain_health_check.utils.DomainHealthCheckUtils;
import com.monitoringsystem.utils.EndpointProps;
import com.monitoringsystem.utils.hashing.Hasher;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/service", templatePath = "", httpMethod = "POST")
public class RegisterService implements HttpHandler
{
    public static boolean dbOp(String sqlQuery, List<Object> sqlParams)
    {
        boolean status = false;
        try
        {
            Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();

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
        String serviceId = Hasher.hashSHA512(serviceName);
        String serviceUrlDomain = formData.getFirst("service_url_domain").getValue();
        String serviceRegistrationStatus = "ACTIVE"; // ACTIVE TERMINAL

        // String svcDiagnosisMethod = formData.getFirst("diagnosis_method").getValue();
        String svcDiagnosisId = formData.getFirst("diagnosis_id").getValue();
        String svcDiagnosisInterval = formData.getFirst("svc_diagnosis_interval").getValue();
        String numOfRetries = formData.getFirst("num_of_retries").getValue();
        String retryIntervalSecs = formData.getFirst("retry_interval_secs").getValue();
        // String svcDiagnosisId = ""; // extract from db.

        X509Certificate cert = DomainHealthCheckUtils.getSslCertificateAndHandshake(serviceUrlDomain)[0];
        String certId = cert.getSerialNumber().toString(16);
        String certIssuer = cert.getIssuerX500Principal().getName();
        Date certExpiryDate = cert.getNotAfter();

        // String certDiagnosisMethod = "TLS_HANDSHAKE";
        Integer certDiagnosisId = 6;
        String certDiagnosisInterval = formData.getFirst("cert_diagnosis_interval").getValue();
        Integer certAlertThreshold = Integer.parseInt(formData.getFirst("alert_threshold_days").getValue());
        
        String svcInfoSqlQuery = """
                INSERT INTO service_info (service_id, service_name, service_url_domain, svc_registration_status) 
                VALUES (?, ?, ?, ?)
            """;

        String svcConfSqlQuery = """
                INSERT INTO service_configs (service_id, svc_diag_id, svc_diagnosis_interval, num_of_retries, retry_interval_secs) 
                VALUES (?, ?, ?, ?, ?)
            """;

        String certInfoSqlQuery = """
                INSERT INTO ssl_certificate_info (service_id, cert_id, issuer, expiry_date) 
                VALUES (?, ?, ?, ?)
            """;

        String certConfSqlQuery = """
                INSERT INTO ssl_certificate_configs (cert_id, svc_diag_id, cert_diagnosis_interval, alert_threshold_days) 
                VALUES (?, ?, ?, ?)
            """;
        
        List<Object> svcInfoSqlParams = List.of(serviceId, serviceName, serviceUrlDomain, serviceRegistrationStatus);
        List<Object> svcConfSqlParams = List.of(serviceId, svcDiagnosisId, svcDiagnosisInterval, numOfRetries, retryIntervalSecs);
        List<Object> certInfoSqlParams = List.of(serviceId, certId, certIssuer, certExpiryDate);
        List<Object> certConfSqlParams = List.of(certId, certDiagnosisId, certDiagnosisInterval, certAlertThreshold);

        String response = "{'status': 'error'}";
        if (dbOp(svcInfoSqlQuery, svcInfoSqlParams) && dbOp(svcConfSqlQuery, svcConfSqlParams) && dbOp(certInfoSqlQuery, certInfoSqlParams) && dbOp(certConfSqlQuery, certConfSqlParams))
        {
            response = "{'status': 'success'}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);

    }
}
