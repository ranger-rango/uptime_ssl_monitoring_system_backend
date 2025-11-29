package com.monitoringsystem;

import com.monitoringsystem.db_handlers.DatabaseWriteConfig;
import com.monitoringsystem.rest.RestApiServer;
import com.monitoringsystem.schedule_manager.TaskScheduler;
import com.monitoringsystem.utils.Constants;

public class Main
{
    static
    {
        DatabaseWriteConfig.writeDbProperties();
        Constants.init();
    }
    public static void main(String[] args)
    {
        RestApiServer.restApiServerStart();
        TaskScheduler.scheduleTasks();
    }
}