package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;
import com.google.common.base.Optional;

import java.util.List;

public class AppServerImpl implements AppServer {

    private static final String APP_SERVER = "AppServer";

    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;
    private boolean recurrentTaskActive;

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

    @Override
    public void sendCommand(AppServerCommand command) {
        Optional<DestinationSpec> destinationSpec = Bus.getMessageService().getDestinationSpec(messagingName());
        if (!destinationSpec.isPresent()) {
            throw new ServerMessageQueueMissing(messagingName());
        }
        String json = Bus.getJsonService().serialize(command);
        destinationSpec.get().message(json).send();
    }

    String messagingName() {
        return APP_SERVER + '_' + getName();
    }

    public boolean isRecurrentTaskActive() {
        return recurrentTaskActive;
    }

    public void setRecurrentTaskActive(boolean recurrentTaskActive) {
        this.recurrentTaskActive = recurrentTaskActive;
    }
}
