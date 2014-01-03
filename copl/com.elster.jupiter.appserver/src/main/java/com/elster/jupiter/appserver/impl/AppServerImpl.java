package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.List;

public class AppServerImpl implements AppServer {

    private static final String APP_SERVER = "AppServer";

    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;
    private boolean recurrentTaskActive;
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final JsonService jsonService;

    @Inject
	AppServerImpl(DataModel dataModel, CronExpressionParser cronExpressionParser, MessageService messageService, JsonService jsonService) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        this.jsonService = jsonService;
    }
    
    AppServerImpl init(String name, CronExpression scheduleFrequency) {
        this.name = name;
        this.scheduleFrequency = scheduleFrequency;
        this.cronString = scheduleFrequency.toString();
        return this;
    }

    static AppServerImpl from(DataModel dataModel, String name, CronExpression scheduleFrequency) {
        return dataModel.getInstance(AppServerImpl.class).init(name, scheduleFrequency);
    }

    @Override
	public SubscriberExecutionSpecImpl createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount) {
        SubscriberExecutionSpecImpl subscriberExecutionSpec = SubscriberExecutionSpecImpl.from(dataModel, this, subscriberSpec, threadCount);
        getSubscriberExecutionSpecFactory().persist(subscriberExecutionSpec);
        return subscriberExecutionSpec;
    }

    private DataMapper<SubscriberExecutionSpec> getSubscriberExecutionSpecFactory() {
        return dataModel.mapper(SubscriberExecutionSpec.class);
    }

    @Override
	public List<SubscriberExecutionSpec> getSubscriberExecutionSpecs() {
    	return getSubscriberExecutionSpecFactory().find("appServer", this);
    }
    
    @Override
    public CronExpression getScheduleFrequency() {
        if (scheduleFrequency == null) {
            scheduleFrequency = cronExpressionParser.parse(cronString);
        }
        return scheduleFrequency;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendCommand(AppServerCommand command) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(messagingName());
        if (!destinationSpec.isPresent()) {
            throw new ServerMessageQueueMissing(messagingName());
        }
        String json = jsonService.serialize(command);
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
