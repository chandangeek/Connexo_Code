package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;

public class SubscriberExecutionSpecInfo {

    public SubscriberSpecInfo subscriberSpec;
    public int numberOfThreads;

    public SubscriberExecutionSpecInfo() {
    }

    private SubscriberExecutionSpecInfo(SubscriberSpecInfo subscriberSpec, int numberOfThreads) {
        this.subscriberSpec = subscriberSpec;
        this.numberOfThreads = numberOfThreads;
    }

    public static SubscriberExecutionSpecInfo of(SubscriberExecutionSpec executionSpec) {
        return new SubscriberExecutionSpecInfo(SubscriberSpecInfo.of(executionSpec), executionSpec.getThreadCount());
    }

    public boolean matches(SubscriberExecutionSpec executionSpec) {
        return subscriberSpec != null && executionSpec != null && subscriberSpec.matches(executionSpec.getSubscriberSpec());
    }
}
