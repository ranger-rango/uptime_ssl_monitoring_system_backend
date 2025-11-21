package com.monitoringsystem.rest_handlers.domain_health_check;

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

@EndpointProps(prefixPath = "/services", templatePath = "/domain-health-check/{serviceId}", httpMethod = "GET")
public class RunSvcHealthCheck implements HttpHandler
{    
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception
    {
        String serviceId = httpServerExchange.getQueryParameters().get("serviceId").getFirst();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Map<String, Object>> serviceConfigs = Constants.SERVICE_CONFIGURATION;
        System.out.println(serviceConfigs);
        Map<String, Object> serviceConfig = serviceConfigs.entrySet().stream()
        .filter(entry -> String.valueOf(entry.getValue().get("service_id")).equals(serviceId))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);

        String diagnosisMethod = String.valueOf(serviceConfig.get("diagnosis_method"));
        String domain = String.valueOf(serviceConfig.get("service_url_domain"));
        Map<String, Object> healthStatusMap = new HashMap<>();
        switch (diagnosisMethod)
        {
            case "PING" -> healthStatusMap = DomainHealthCheckUtils.checkIcmpReachability(domain);
            case "REQUEST" -> healthStatusMap = DomainHealthCheckUtils.checkFullPageLoad(domain);
            case "PORT_SCAN" -> healthStatusMap = DomainHealthCheckUtils.checkTcpPortConnectivity(domain, 80);
            case "HEAD" -> healthStatusMap = DomainHealthCheckUtils.headRequest(domain);
            case "OPTIONS" -> healthStatusMap = DomainHealthCheckUtils.optionsRequest(domain);
            default -> throw new IllegalArgumentException("Invalid diagnosis method: " + domain);
        }
        String response = objectMapper.writeValueAsString(healthStatusMap);

        List<Object> sqlParams = List.of(
            serviceId, 
            healthStatusMap.get("svc_health_status"), 
            healthStatusMap.get("response_time_ms"), 
            healthStatusMap.get("response_code"), 
            healthStatusMap.get("check_at"),
            healthStatusMap.get("error_message"));
        Logger.svcCheckLogger(sqlParams);

        httpServerExchange.setStatusCode(200);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        httpServerExchange.getResponseSender().send(response);
    }

}
