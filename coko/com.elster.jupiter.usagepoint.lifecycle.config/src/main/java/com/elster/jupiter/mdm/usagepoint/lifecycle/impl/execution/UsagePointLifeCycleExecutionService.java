package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface UsagePointLifeCycleExecutionService {

    void triggerTransition(UsagePoint usagePoint, UsagePointTransition transition, Map<String, Object> properties, Instant transitionTime);
}
