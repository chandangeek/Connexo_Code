package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.UsagePointLifeCycleExecutionService;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;

public interface ServerUsagePointLifeCycleExecutionService extends UsagePointLifeCycleExecutionService {

    void performTransition(UsagePoint usagePoint, UsagePointState targetState, Instant transitionTime);
}
