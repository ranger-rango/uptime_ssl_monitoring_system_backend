package com.monitoringsystem.db_handlers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnectionsHikari
{
    private static HikariDataSource dataSource;

    static
    {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("sys.conf/db.properties"))
        {
            properties.load(input);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        HikariConfig hikariConfig = new HikariConfig(properties);
        dataSource = new HikariDataSource(hikariConfig);
    }

    public static HikariDataSource getDbDataSource()
    {
        return dataSource;
    }

    public static void closeDbDataSource()
    {
        if (dataSource != null && !dataSource.isClosed())
        {
            dataSource.close();
        }
    }

}
