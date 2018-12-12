/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;

public class SubscriberExecutionSpecInfo {

    public SubscriberSpecInfo subscriberSpec;
    public int numberOfThreads;
    public boolean active;

    public SubscriberExecutionSpecInfo() {
    }

    private SubscriberExecutionSpecInfo(SubscriberSpecInfo subscriberSpec, int numberOfThreads, boolean active) {
        this.subscriberSpec = subscriberSpec;
        this.numberOfThreads = numberOfThreads;
        this.active = active;
    }

    public static SubscriberExecutionSpecInfo of(SubscriberExecutionSpec executionSpec) {
        return new SubscriberExecutionSpecInfo(SubscriberSpecInfo.of(executionSpec), executionSpec.getThreadCount(), executionSpec.isActive());
    }

    public boolean matches(SubscriberExecutionSpec executionSpec) {
        return subscriberSpec != null && executionSpec != null && subscriberSpec.matches(executionSpec.getSubscriberSpec());
    }
}
