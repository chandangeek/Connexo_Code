/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;

public class SubscriberExecutionSpecInfo {

    public int numberOfThreads;
    public boolean active;
    public String appServerName;

    public SubscriberExecutionSpecInfo() {
    }

    private SubscriberExecutionSpecInfo(SubscriberExecutionSpec subscriberExecutionSpec) {
        this.numberOfThreads = subscriberExecutionSpec.getThreadCount();
        this.active = subscriberExecutionSpec.isActive() && subscriberExecutionSpec.getAppServer().isActive();
        this.appServerName = subscriberExecutionSpec.getAppServer().getName();
    }

    public static SubscriberExecutionSpecInfo of(SubscriberExecutionSpec executionSpec) {
        return new SubscriberExecutionSpecInfo(executionSpec);
    }

}
