package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ServerMessageQueueMissing;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class AppServerImpl implements AppServer {

    private static final String APP_SERVER = "AppServer";

    private String name;
    private String cronString;
    private transient CronExpression scheduleFrequency;
    private boolean recurrentTaskActive = true;
    private boolean active;
    private final DataModel dataModel;
    private final CronExpressionParser cronExpressionParser;
    private final MessageService messageService;
    private final JsonService jsonService;
    private final Thesaurus thesaurus;

    @Inject
	AppServerImpl(DataModel dataModel, CronExpressionParser cronExpressionParser, MessageService messageService, JsonService jsonService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.cronExpressionParser = cronExpressionParser;
        this.messageService = messageService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
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
        sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
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
            scheduleFrequency = cronExpressionParser.parse(cronString).orElseThrow(IllegalArgumentException::new);
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
            throw new ServerMessageQueueMissing(messagingName(), thesaurus);
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

    @Override
    public void setRecurrentTaskActive(boolean recurrentTaskActive) {
        this.recurrentTaskActive = recurrentTaskActive;
    }


    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        if (!active) {
            active = true;
            dataModel.mapper(AppServer.class).update(this);
            sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
        }
    }

    @Override
    public void deactivate() {
        if (active) {
            active = false;
            dataModel.mapper(AppServer.class).update(this);
            sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
        }
    }

    @Override
    public void removeSubscriberExecutionSpec(SubscriberExecutionSpec subscriberExecutionSpec) {
        SubscriberExecutionSpec toRemove = getSubscriberExecutionSpecs().stream()
                .filter(sp -> sp.getSubscriberSpec().getDestination().getName().equals(subscriberExecutionSpec.getSubscriberSpec().getDestination().getName()))
                .filter(sp -> sp.getSubscriberSpec().getName().equals(subscriberExecutionSpec.getSubscriberSpec().getName()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        getSubscriberExecutionSpecFactory().remove(toRemove);
        sendCommand(new AppServerCommand(Command.CONFIG_CHANGED));
    }
}
