/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Map;

public interface ServerUsagePointLifeCycleService extends UsagePointLifeCycleService {
    String DESTINATION_NAME = "UsagePointLifeCycle";
    String QUEUE_SUBSCRIBER = "UpeQueueSubscriber";
    String EXECUTOR_TASK = "Usage point state change task";

    void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime)
            throws ExecutableMicroCheckException;

    void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties)
            throws ExecutableMicroActionException;

    void performTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime);

    DataModel getDataModel();

    void rescheduleExecutor();

    void createUsagePointInitialStateChangeRequest(UsagePoint usagePoint);

    User getCurrentUser();
}
