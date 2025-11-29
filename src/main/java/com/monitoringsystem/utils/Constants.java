package com.monitoringsystem.utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoringsystem.db_handlers.DatabaseConnectionsHikari;
import com.monitoringsystem.db_handlers.DatabaseOperationsHikari;
import com.monitoringsystem.db_handlers.DatabaseResultsProcessors;

record XmlConfig (XPath xpath, Document document) {}

public class Constants
{
    public static String DEFAULT_ADMIN_AUTH_TOKEN; // = getDefaultAdminAuthToken();
    public static final String UDERTOW_PORT = getUndertowPort();
    public static final String UNDERTOW_HOST = getUndertowHost();
    public static final String UNDERTOW_BASE_PATH_REST = getUndertowBasePathRest();
    public static final String DB_HOST = getDbHost();
    public static final String DB_PORT = getDbPort();
    public static final String DATABASE_NAME = getDatabaseName();
    public static final String DB_USERNAME = getDbUserName();
    public static final String DB_PASSWORD = getDbPassword();
    public static volatile Map<String, Map<String, Object>> SERVICE_CONFIGURATION;
    public static final String SMTP_SERVER = getSmtpServer();
    public static final String SMTP_SERVER_PORT = getSmtpServerPort();
    public static final String SMTP_SERVER_USERNAME = getSmtpServerUsername();
    public static final String SMTP_SERVER_PASSWORD = getSmtpServerPassword();
    public static final String FRONTEND_DOMAIN = getFrontEndDomain();
    public static final String FRONTEND_REG_USER_ENDPOINT = getFrontEndRegistrationEndpoint();

    public static void init()
    {
        load();
        startRefreshThread();
        DEFAULT_ADMIN_AUTH_TOKEN = getDefaultAdminAuthToken();

    }

    public static XmlConfig readXmlConfigFile() throws IOException, ParserConfigurationException, SAXException
    {
        Path configPath = Paths.get("sys.conf/conf.xml");
        String xmlString = new String(Files.readAllBytes(configPath));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        Document doc = builder.parse(is);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        return new XmlConfig(xPath, doc);
    }

    public static String getDatabaseName()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DB/DATABASE_NAME");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDbUserName()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DB/USERNAME");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDbPassword()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DB/PASSWORD");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDbHost()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DB/HOST");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDbPort()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DB/PORT");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUndertowHost()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//UNDERTOW/HOST/@REST");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUndertowPort()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//UNDERTOW/PORT/@REST");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUndertowBasePathRest()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//UNDERTOW/BASE_PATH/@REST");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Object>> getServiceConfigs()
    {
        Map<String, Map<String, Object>> map = new HashMap<>();
        String sqlQuery = """
                SELECT si.service_id, si.service_url_domain, sdm.diagnosis_method, sc.svc_diagnosis_interval, sc.num_of_retries, sc.retry_interval_secs, sci.cert_id, sci.issuer, sci.expiry_date, sci.is_cert_active_status, scc.cert_diagnosis_interval, scc.alert_threshold_days
                FROM service_info si
                JOIN service_configs sc ON si.service_id = sc.service_id 
				JOIN service_diagnosis_methods sdm ON sc.svc_diag_id = sdm.svc_diag_id
				JOIN ssl_certificate_info sci ON sci.service_id = si.service_id
				JOIN ssl_certificate_configs scc ON sci.cert_id = scc.cert_id
                WHERE si.svc_registration_status = 'ACTIVE' AND sci.is_cert_active_status = 'ACTIVE'
                """;
        List<Object> sqlParams = List.of();
        Connection connection = null;
        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet != null)
            {
                String jsonString = DatabaseResultsProcessors.processResultsToJson(resultSet, connection);
                ObjectMapper objectMapper = new ObjectMapper();
                map = objectMapper.readValue(jsonString, Map.class);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        return map;
    }

    private static void load()
    {
        SERVICE_CONFIGURATION = getServiceConfigs();
    }

    private static void startRefreshThread()
    {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    load();
                } catch (Exception ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static String getSmtpServer()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//SMTP/DOMAIN");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSmtpServerPort()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//SMTP/PORT");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSmtpServerUsername()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//SMTP/EMAIL");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSmtpServerPassword()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//SMTP/APP_PASSWORD");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFrontEndDomain()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//FRONTEND/DOMAIN");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFrontEndRegistrationEndpoint()
    {
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//FRONTEND/REGISTER_USER_ENDPOINT");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            return nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDefaultAdminAuthToken()
    {
        String defaultAdmin = "";
        try
        {
            XmlConfig xmlConfig = readXmlConfigFile();
            XPathExpression expr = xmlConfig.xpath().compile("//DEFAULT/ADMIN_EMAIL");
            NodeList nodeList = (NodeList) expr.evaluate(xmlConfig.document(), XPathConstants.NODESET);
            defaultAdmin = nodeList.item(0).getTextContent();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        String sqlQuery = """
                SELECT auth_token
                FROM user_auth_tokens
                WHERE email_address = ?
                """;
        List<Object> sqlParams = List.of(defaultAdmin);
        String defaultAdminAuthToken = "";
        Connection connection = null;
        try
        {
            connection = DatabaseConnectionsHikari.getDbDataSource().getConnection();
            ResultSet resultSet = DatabaseOperationsHikari.dbQuery(connection, sqlQuery, sqlParams);
            if (resultSet != null && resultSet.next())
            {
                defaultAdminAuthToken = resultSet.getString("auth_token");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return defaultAdminAuthToken;
    }


}