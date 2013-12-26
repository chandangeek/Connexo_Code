package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class SubscriberExecutionSpecImpl implements SubscriberExecutionSpec {

    @SuppressWarnings("unused")
	private long id;
    private int threadCount;
    private String subscriberSpecName;
    private String destinationSpecName;
    private transient SubscriberSpec subscriberSpec;
    private String appServerName;
    private transient AppServer appServer;
    private final DataModel dataModel;
    private final MessageService messageService;

    @SuppressWarnings("unused")
    @Inject
	SubscriberExecutionSpecImpl(DataModel dataModel, MessageService messageService) {
    	this.dataModel = dataModel;
        this.messageService = messageService;
    }
    
    public static SubscriberExecutionSpecImpl from(DataModel dataModel, MessageService messageService, AppServer appServer, SubscriberSpec subscriberSpec, int threadCount) {
        SubscriberExecutionSpecImpl subscriberExecutionSpec = new SubscriberExecutionSpecImpl(dataModel, messageService);
        return subscriberExecutionSpec.init(appServer, subscriberSpec, threadCount);
    }

    SubscriberExecutionSpecImpl init(AppServer appServer, SubscriberSpec subscriberSpec, int threadCount) {
        this.appServer = appServer;
        this.appServerName = appServer.getName();
        this.subscriberSpec = subscriberSpec;
        this.subscriberSpecName = subscriberSpec.getName();
        this.destinationSpecName = subscriberSpec.getDestination().getName();
        this.threadCount = threadCount;
        return this;
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public SubscriberSpec getSubscriberSpec() {
        if (subscriberSpec == null) {
            subscriberSpec = messageService.getSubscriberSpec(destinationSpecName, subscriberSpecName).get();
        }
        return subscriberSpec;
    }

    @Override
    public AppServer getAppServer() {
        if (appServer == null) {
            appServer = dataModel.mapper(AppServer.class).getOptional(appServerName).get();
        }
        return appServer;
    }
}
