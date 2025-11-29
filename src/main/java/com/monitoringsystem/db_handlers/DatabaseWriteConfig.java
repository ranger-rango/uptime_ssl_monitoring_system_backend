package com.monitoringsystem.db_handlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.monitoringsystem.utils.Constants;

public class DatabaseWriteConfig
{
    public static void writeDbProperties()
    {
        String dbUserName = Constants.DB_USERNAME;
        String dbPassword = Constants.DB_PASSWORD;
        String databaseName = Constants.DATABASE_NAME;
        String databaseHost = Constants.DB_HOST;
        String databasePort = Constants.DB_PORT;
        
        String dbProperties = String.format(
            """
                # PostgreSQL DataSource settings
                dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
                dataSource.user=%s
                dataSource.password=%s 
                dataSource.databaseName=%s 

                dataSource.serverName=%s
                dataSource.portNumber=%s

                # HikariCP optional tuning
                # maximumPoolSize=10
                # minimumIdle=2
                # connectionTimeout=30000
                # idleTimeout=600000
                # maxLifetime=1800000
                # poolName=PostgresHikariPool

                # windows psql conf file: C:\\Program Files\\PostgreSQL\\17\\data
                """, dbUserName, dbPassword, databaseName, databaseHost, databasePort);
        
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("db.properties")))
        {
            bufferedWriter.write(dbProperties);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void deleteDbProperties()
    {
        Path dbPropertiesPath = Paths.get("db.properties");
        try
        {
            Files.deleteIfExists(dbPropertiesPath);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
