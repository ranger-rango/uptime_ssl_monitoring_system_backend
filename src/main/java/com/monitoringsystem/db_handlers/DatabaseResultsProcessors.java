package com.monitoringsystem.db_handlers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class DatabaseResultsProcessors
{
    public static Map<Integer, Map<String, Object>> resultSetMapper(ResultSet resultSet) throws SQLException, JsonProcessingException
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
        return recordsMap;
    }

    public static String processResultsToJson(ResultSet resultSet) throws SQLException, JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(resultSetMapper(resultSet));
    }

    public static String processResultsToXml(ResultSet resultSet) throws SQLException, JsonProcessingException
    {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writer().withRootName("QueryResult").writeValueAsString(resultSetMapper(resultSet));
    }

    public static void processResultsAndPrint(ResultSet resultSet) throws SQLException, JsonProcessingException
    {
        resultSetMapper(resultSet).entrySet().stream().forEach(entry -> 
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