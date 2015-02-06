package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.nls.Thesaurus;

public class SubscriberExecutionSpecInfo {

    public SubscriberSpecInfo subscriberSpec;
    public int numberOfThreads;

    public SubscriberExecutionSpecInfo() {
    }

    private SubscriberExecutionSpecInfo(SubscriberSpecInfo subscriberSpec, int numberOfThreads) {
        this.subscriberSpec = subscriberSpec;
        this.numberOfThreads = numberOfThreads;
    }

    public static SubscriberExecutionSpecInfo of(SubscriberExecutionSpec executionSpec, Thesaurus thesaurus) {
        return new SubscriberExecutionSpecInfo(SubscriberSpecInfo.of(executionSpec, thesaurus), executionSpec.getThreadCount());
    }

    public boolean matches(SubscriberExecutionSpec executionSpec) {
        return subscriberSpec != null && executionSpec != null && subscriberSpec.matches(executionSpec.getSubscriberSpec());
    }
}
