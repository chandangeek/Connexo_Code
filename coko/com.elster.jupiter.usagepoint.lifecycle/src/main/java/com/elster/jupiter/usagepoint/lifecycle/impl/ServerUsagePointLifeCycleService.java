package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import java.time.Instant;
import java.util.Map;

public interface ServerUsagePointLifeCycleService extends UsagePointLifeCycleService {
    void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties)
            throws ExecutableMicroCheckException;

    void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties)
            throws ExecutableMicroActionException;

    void performTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime);
}
