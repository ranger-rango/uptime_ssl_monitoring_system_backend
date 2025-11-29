package com.monitoringsystem.rest_handlers.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.EndpointProps;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

@EndpointProps(prefixPath = "/services", templatePath = "", httpMethod = "GET", allowedRoles = {"ADMIN", "OPERATOR"})
public class GetServices implements HttpHandler
{
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        Connection connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
        String sqlQuery = """
        SELECT si.service_id, si.service_name, si.service_url_domain, si.svc_registration_status, sdm.diagnosis_method, sc.svc_diagnosis_interval, sc.num_of_retries, sc.retry_interval_secs, sci.cert_id, sci.issuer, sci.expiry_date, sci.is_cert_active_status, scc.cert_diagnosis_interval, scc.alert_threshold_days
        FROM service_info si
        JOIN service_configs sc ON si.service_id = sc.service_id 
        JOIN service_diagnosis_methods sdm ON sc.svc_diag_id = sdm.svc_diag_id
        JOIN ssl_certificate_info sci ON sci.service_id = si.service_id
        JOIN ssl_certificate_configs scc ON sci.cert_id = scc.cert_id
        WHERE sci.is_cert_active_status = 'ACTIVE' --AND si.svc_registration_status = 'ACTIVE'
            """;
        List<Object> sqlParams = List.of();
        ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
        String response = "";
        if (resultSet != null)
        {
            response = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
        }
        else
        {
            response = "{\"err_status\" : \"Services fetch failed\"}";
        }

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }
}
