package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;

import java.util.List;

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
	public SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
        SubscriberExecutionSpecImpl subscriberExecutionSpec = new SubscriberExecutionSpecImpl(this, subscriberSpec, threadCount);
        Bus.getOrmClient().getSubscriberExecutionSpecFactory().persist(subscriberExecutionSpec);
        return subscriberExecutionSpec;
    }
    
    @Override
	public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
    	return Bus.getOrmClient().getSubscriberExecutionSpecFactory().find("appServer", this);
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
