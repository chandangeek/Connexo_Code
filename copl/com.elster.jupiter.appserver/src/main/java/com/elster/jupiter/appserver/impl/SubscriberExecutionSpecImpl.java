package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;

public class SubscriberExecutionSpecImpl implements SubscriberExecutionSpec {

    private long id;
    private int threadCount;
    private String subscriberSpecName;
    private String destinationSpecName;
    private transient SubscriberSpec subscriberSpec;
    private String appServerName;
    private transient AppServer appServer;

    private SubscriberExecutionSpecImpl() {
    	
    }
    
    public SubscriberExecutionSpecImpl(AppServer appServer, SubscriberSpec subscriberSpec, int threadCount) {
        this.appServer = appServer;
        this.appServerName = appServer.getName();
        this.subscriberSpec = subscriberSpec;
        this.subscriberSpecName = subscriberSpec.getName();
        this.destinationSpecName = subscriberSpec.getDestination().getName();
        this.threadCount = threadCount;
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public SubscriberSpec getSubscriberSpec() {
        if (subscriberSpec == null) {
            subscriberSpec = Bus.getMessageService().getSubscriberSpec(destinationSpecName, subscriberSpecName).get();
        }
        return subscriberSpec;
    }

    @Override
    public AppServer getAppServer() {
        if (appServer == null) {
            appServer = Bus.getOrmClient().getAppServerFactory().get(appServerName).get();
        }
        return appServer;
    }
}
