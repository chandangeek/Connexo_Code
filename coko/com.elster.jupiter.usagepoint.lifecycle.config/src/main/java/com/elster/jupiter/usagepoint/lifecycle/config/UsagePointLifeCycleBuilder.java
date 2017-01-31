/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

/**
 * Allows for external bundles configure an instance of default usage point lifecycle.
 * Implementation can be registered via call of {@link UsagePointLifeCycleConfigurationService#addUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder)}
 * or by defining the UsagePointLifeCycleBuilder service instance.
 */
@ConsumerType
public interface UsagePointLifeCycleBuilder {

    /**
     * This method is called from inside of {@link UsagePointLifeCycleConfigurationService#newUsagePointLifeCycle(String)}.
     * Realisation can create transitions, states, assign micro actions and checks.
     *
     * @param usagePointLifeCycle saved instance of default usage point lifecycle with initial state {@link DefaultState#UNDER_CONSTRUCTION},
     * this life cycle can be already modified by another implementation.
     */
    void accept(UsagePointLifeCycle usagePointLifeCycle);
}
