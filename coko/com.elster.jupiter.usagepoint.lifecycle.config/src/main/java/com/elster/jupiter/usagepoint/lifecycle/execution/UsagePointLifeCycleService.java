package com.elster.jupiter.usagepoint.lifecycle.execution;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Map;

@ProviderType
public interface UsagePointLifeCycleService {
    String COMPONENT_NAME = "UPE";

    void triggerTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, Map<String, Object> properties);
}
