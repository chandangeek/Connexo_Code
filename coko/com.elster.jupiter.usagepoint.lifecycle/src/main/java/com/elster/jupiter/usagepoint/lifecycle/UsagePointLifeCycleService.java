/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface UsagePointLifeCycleService {
    String COMPONENT_NAME = "UPE";

    UsagePointStateChangeRequest performTransition(UsagePoint usagePoint, UsagePointTransition transition, String application, Map<String, Object> properties);

    UsagePointStateChangeRequest scheduleTransition(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, String application, Map<String, Object> properties);

    List<UsagePointStateChangeRequest> getHistory(UsagePoint usagePoint);

    Optional<UsagePointStateChangeRequest> getLastUsagePointStateChangeRequest(UsagePoint usagePoint);

    List<UsagePointTransition> getAvailableTransitions(UsagePointState state, String application);
}
