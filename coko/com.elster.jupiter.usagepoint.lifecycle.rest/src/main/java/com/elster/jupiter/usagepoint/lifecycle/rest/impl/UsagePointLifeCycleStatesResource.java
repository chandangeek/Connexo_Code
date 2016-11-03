package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import javax.inject.Inject;

public class UsagePointLifeCycleStatesResource {
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public UsagePointLifeCycleStatesResource(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }
}
