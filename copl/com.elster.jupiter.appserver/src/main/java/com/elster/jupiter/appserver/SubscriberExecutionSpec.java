package com.elster.jupiter.appserver;

import com.elster.jupiter.messaging.SubscriberSpec;

public interface SubscriberExecutionSpec {

    int getThreadCount();

    SubscriberSpec getSubscriberSpec();

    AppServer getAppServer();

    void setActive(boolean active);

    boolean isActive();

}
