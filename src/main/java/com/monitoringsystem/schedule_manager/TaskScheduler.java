package com.monitoringsystem.schedule_manager;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;
import com.monitoringsystem.utils.Constants;
import com.monitoringsystem.utils.HttpRequestHandler;
import com.monitoringsystem.utils.Logger;
import com.monitoringsystem.utils.notification_manager.Mailer;

public class TaskScheduler
{
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private static String undertowHost = Constants.UNDERTOW_HOST;
    private static String undertowPort = Constants.UDERTOW_PORT;
    private static String undertowBaseUrl = Constants.UNDERTOW_BASE_PATH_REST;

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Object>> dbQueryHelper(String sqlQuery, List<Object> sqlParams)
    {
        Connection connection = null;
        String resultString = "";
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            
            if (resultSet != null)
            {
                resultString = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
                resultMap = objectMapper.readValue(resultString, Map.class);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally 
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return resultMap;
    }

    public static void scheduleTasks()
    {
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, Object>> servicesMap = Constants.SERVICE_CONFIGURATION;

        servicesMap.entrySet().stream().forEach(entry -> 
        {
            Map<String, Object> serviceMap = entry.getValue();
            String serviceId = (String) serviceMap.get("service_id");
            Integer diagnosisInterval = (Integer) serviceMap.get("svc_diagnosis_interval");
            Integer numberOfRetries = (Integer) serviceMap.get("num_of_retries");
            Integer retryInterval = (Integer) serviceMap.get("retry_interval_secs") * 1000;
            Integer sslDiagnosisInterval = (Integer) serviceMap.get("cert_diagnosis_interval");
            
            String sqlQuery = """
                    SELECT scg.service_id, cgm.user_id, su.channel_id, su.email_address, su.first_name, su.surname
                    FROM service_contact_groups scg
                    JOIN contact_group_members cgm ON cgm.contact_group_id = scg.contact_group_id
                    JOIN system_users su ON su.user_id = cgm.user_id
                    WHERE scg.service_id = ?
                    """;
            List<Object> sqlParams = List.of(serviceId);
            var contactGroupData = dbQueryHelper(sqlQuery, sqlParams);

            @SuppressWarnings("unchecked")
            Runnable domainHealthTask = () -> 
            {
                try
                {
                    URI domainhealthCheckUri = new URI(String.format("http://%s:%s%s/services/domain-health-check/%s", undertowHost, undertowPort, undertowBaseUrl, serviceId));
                    String healthCheckResponse = "";
                    Map<String, Object> healthCheckResponseMap = new HashMap<>();
                    String healthCheckStatus = "";
                    for (int i = 0; i <= numberOfRetries; i++)
                    {
                        if (i > 0)
                        {
                            try
                            {
                                Thread.sleep(retryInterval);
                            }
                            catch(InterruptedException e)
                            {
                                e.printStackTrace();
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        healthCheckResponse = HttpRequestHandler.fetchRequest(domainhealthCheckUri);
                        if (healthCheckResponse == null || healthCheckResponse.isEmpty())
                        {
                            return;
                        }
                        healthCheckResponseMap = objectMapper.readValue(healthCheckResponse, Map.class);
                        healthCheckStatus = (String) healthCheckResponseMap.get("svc_health_status");
                        if ("UP".equals(healthCheckStatus))
                        {
                            break;
                        }
                    }

                    if ("DOWN".equals(healthCheckStatus)) // SVC_DOWN notification
                    {
                        String notificationTriggerQuery = """
                                SELECT notification_trigger_id
                                FROM notification_triggers
                                WHERE notification_trigger = ?
                                """;
                        List<Object> notificationTriggerParams = List.of("SVC_DOWN");
                        var notTriggerResults = dbQueryHelper(notificationTriggerQuery, notificationTriggerParams);
                        Integer notificationTriggerId = (Integer) notTriggerResults.get("1").get("notification_trigger_id");


                        contactGroupData.entrySet().stream().forEach(dataEntry -> 
                        {
                            Map<String, Object> contactGroupMember = dataEntry.getValue();
                            String recipientEmail = (String) contactGroupMember.get("email_address");
                            String recipientName = (String) contactGroupMember.get("first_name") + (String) contactGroupMember.get("surname");
                            String serviceUrl = (String) contactGroupMember.get("service_url_domain");
                            String notificationTrigger = "SVC_DOWN";
                            String userId = (String) contactGroupMember.get("user_id");
                            String channelId = (String) contactGroupMember.get("channel_id");
                            LocalDateTime createAt = LocalDateTime.now();
                            List<Object> alertQueryParams = List.of(notificationTriggerId, userId, channelId, createAt);
                            
                            Logger.notificationLogger(alertQueryParams);
                            Mailer.sendEmailNotification("service-error-notification.html", recipientEmail, recipientName, serviceUrl, notificationTrigger);
                        });

                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            };
            scheduledExecutorService.scheduleWithFixedDelay(domainHealthTask, 20, diagnosisInterval, TimeUnit.SECONDS);

            @SuppressWarnings("unchecked")
            Runnable sslCertHealthTask = () -> 
            {
                try
                {
                    URI domainhealthCheckUri = new URI(String.format("http://%s:%s%s/services/ssl-cert-health-check/%s", undertowHost, undertowPort, undertowBaseUrl, serviceId));
                    String healthCheckResponse = HttpRequestHandler.fetchRequest(domainhealthCheckUri);
                    if (healthCheckResponse == null || healthCheckResponse.isEmpty())
                    {
                        return;
                    }
                    Map<String, Object> responseMap = objectMapper.readValue(healthCheckResponse, Map.class);
                    String certHealthStatus = (String) responseMap.get("cert_health_status");

                    if ("WATCH".equals(certHealthStatus)) // SSL_CERT_EXPIRY notification
                    {
                        String notificationTriggerQuery = """
                                SELECT notification_trigger_id
                                FROM notification_triggers
                                WHERE notification_trigger = ?
                                """;
                        List<Object> notificationTriggerParams = List.of("SSL_CERT_EXPIRY");
                        var notTriggerResults = dbQueryHelper(notificationTriggerQuery, notificationTriggerParams);
                        Integer notificationTriggerId = (Integer) notTriggerResults.get("1").get("notification_trigger_id");

                        contactGroupData.entrySet().stream().forEach(dataEntry -> 
                        {
                            Map<String, Object> contactGroupMember = dataEntry.getValue();
                            String recipientEmail = (String) contactGroupMember.get("email_address");
                            String recipientName = (String) contactGroupMember.get("first_name") + (String) contactGroupMember.get("surname");
                            String serviceUrl = (String) contactGroupMember.get("service_url_domain");
                            String notificationTrigger = "SSL_CERT_EXPIRY";
                            String userId = (String) contactGroupMember.get("user_id");
                            String channelId = (String) contactGroupMember.get("channel_id");
                            LocalDateTime createAt = LocalDateTime.now();
                            List<Object> alertQueryParams = List.of(notificationTriggerId, userId, channelId, createAt);
                            
                            Logger.notificationLogger(alertQueryParams);
                            Mailer.sendEmailNotification("service-error-notification.html", recipientEmail, recipientName, serviceUrl, notificationTrigger);

                        });
                    }
                    
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            };
            scheduledExecutorService.scheduleWithFixedDelay(sslCertHealthTask, 20, sslDiagnosisInterval, TimeUnit.SECONDS);

        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            scheduledExecutorService.shutdown();
        }));
    }
}