/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    private boolean active;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
	SubscriberExecutionSpecImpl(DataModel dataModel, MessageService messageService) {
    	this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public static SubscriberExecutionSpecImpl newActive(DataModel dataModel, AppServer appServer, SubscriberSpec subscriberSpec, int threadCount) {
        return dataModel.getInstance(SubscriberExecutionSpecImpl.class).init(appServer, subscriberSpec, threadCount, true);
    }

    public static SubscriberExecutionSpecImpl newInactive(DataModel dataModel, AppServer appServer, SubscriberSpec subscriberSpec, int threadCount) {
        return dataModel.getInstance(SubscriberExecutionSpecImpl.class).init(appServer, subscriberSpec, threadCount, false);
    }

    SubscriberExecutionSpecImpl init(AppServer appServer, SubscriberSpec subscriberSpec, int threadCount, boolean active) {
        this.appServer = appServer;
        this.appServerName = appServer.getName();
        this.subscriberSpec = subscriberSpec;
        this.subscriberSpecName = subscriberSpec.getName();
        this.destinationSpecName = subscriberSpec.getDestination().getName();
        this.threadCount = threadCount;
        this.active = active;
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

    void setThreadCount(int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("Cannot have fewer than 1 thread assigned.");
        }
        this.threadCount = threadCount;
    }

    void update() {
        dataModel.mapper(SubscriberExecutionSpecImpl.class).update(this);
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
