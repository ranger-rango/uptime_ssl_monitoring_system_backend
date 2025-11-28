package com.monitoringsystem.db_handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class DatabaseResultsProcessors
{
    public static Map<Integer, Map<String, Object>> resultSetMapper(ResultSet resultSet, Connection connection) throws SQLException, JsonProcessingException
    {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int cols = resultSetMetaData.getColumnCount();
        Map<Integer, Map<String, Object>> recordsMap = new HashMap<>();
        int j = 1;
        while (resultSet.next())
        {
            Map<String, Object> recordMap = new HashMap<>();
            for (int i = 1; i <= cols; i++)
            {
                recordMap.put(resultSetMetaData.getColumnLabel(i), resultSet.getObject(i));
            }
            recordsMap.put(j, recordMap);
            j++;
        }
        resultSet.close();
        connection.close();
        return recordsMap;
    }

    public static String processResultsToJson(ResultSet resultSet, Connection connection) throws SQLException, JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.writeValueAsString(resultSetMapper(resultSet, connection));
    }

    public static String processResultsToXml(ResultSet resultSet, Connection connection) throws SQLException, JsonProcessingException
    {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writer().withRootName("QueryResult").writeValueAsString(resultSetMapper(resultSet, connection));
    }

    public static void processResultsAndPrint(ResultSet resultSet, Connection connection) throws SQLException, JsonProcessingException
    {
        resultSetMapper(resultSet, connection).entrySet().stream().forEach(entry -> 
        {
            Map<String, Object> map = entry.getValue();
            System.out.print(entry.getKey() + ": " );
            map.entrySet().stream().forEach(mapEntry -> 
            {
                System.out.print(mapEntry.getKey() + ": " + mapEntry.getValue());
            });
        });
    }

}