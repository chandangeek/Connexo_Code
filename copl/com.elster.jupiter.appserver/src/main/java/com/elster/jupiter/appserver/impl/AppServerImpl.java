package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.util.cron.CronExpression;

public class AppServerImpl implements AppServer {

    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;

    private AppServerImpl() {
    	
    }
    
    public AppServerImpl(String name, CronExpression scheduleFrequency) {
        this.name = name;
        this.scheduleFrequency = scheduleFrequency;
        this.cronString = scheduleFrequency.toString();
    }

    @Override
    public CronExpression getScheduleFrequency() {
        if (scheduleFrequency == null) {
            scheduleFrequency = Bus.getCronExpressionParser().parse(cronString);
        }
        return scheduleFrequency;
    }

    @Override
    public String getName() {
        return name;
    }

}
