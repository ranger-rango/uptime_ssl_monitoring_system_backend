package com.monitoringsystem;

import com.monitoringsystem.db_handlers.DatabaseWriteConfig;
import com.monitoringsystem.rest.RestApiServer;
import com.monitoringsystem.schedule_manager.TaskScheduler;

public class Main
{
    static
    {
        DatabaseWriteConfig.writeDbProperties();
        // TaskScheduler.scheduleTasks();
    }
    public static void main(String[] args)
    {
        RestApiServer.restApiServerStart();
        TaskScheduler.scheduleTasks();
    }
}